package org.filteredpush.dataentry.configuration;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

class ConfigurationBase {
	
	private Document doc;
	
	public ConfigurationBase(Document doc) {
		this.doc = doc;
	}

	private static final String ITEM = "item";
	static final String KEY = "key";
	
	public String toString() {
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		try {
			TransformerFactory.newInstance().newTransformer().transform(domSource, result);
		} catch (TransformerException e) {
			throw new Error(e);
		}
		return writer.toString();
	}
	
	private Node getNode(String name) {
		NodeList nodes = doc.getElementsByTagName(name);
		if (nodes.getLength() > 1) {
			throw new IllegalArgumentException("There should only be one '"+name+"' in document.");
		}
		Node node = doc.getElementsByTagName(name).item(0);
		if (node == null) {
			throw new NoSuchElementException("Couldn't find element named '"+name+"'");
		}
		return node;
	}
	
	private String getString(Node node) {
		return node.getTextContent().trim();
	}

	String getString(String name) {
		return getString(getNode(name));
	}
	
	private String getXml(Node node) {
		StringWriter writer = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerFactoryConfigurationError e) {
			throw new Error(e);
		} catch (TransformerException e) {
			throw new Error(e);
		}
		return writer.toString().replaceFirst("^(?s)<\\?[^>]*><(\\S+)[^>]*>(.*)</\\1>$", "$2").trim();
		// TODO: Better way to get the innerHtml?
	}
	
	String getXml(String name) {
		return getXml(getNode(name));
	}
	
	boolean getBoolean(String name) {
		Node node;
		try {
			node = getNode(name);
		} catch (NoSuchElementException e) {
			return false;
		}
		String content = node.getTextContent().trim().toLowerCase();
		if ("false".equals(content)) {
			return false;
		}
		if ("true".equals(content)) {
			return true;
		}
		throw new Error("Boolean element '"+name+"' is neither true nor false.");
	}
	
	/* getList */
	
	private List<String> getList(Node node) {
		List<String> list = new ArrayList<String>();
		for (Node item : new ItemIterable(node)) {
			list.add(item.getTextContent().trim());
		}
		return ImmutableList.copyOf(list);
	}
	
	List<String> getList(String name) {
		return getList(getNode(name));
	}
	
	/* getMap */
	
	private Map<String,String> getMap(Node node) {
		ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
		for (Node item : new ItemIterable(node)) {
			String key = item.getAttributes().getNamedItem(KEY).getNodeValue();
			String value = item.getTextContent().trim();
			builder.put(key, value);
		}
		return builder.build();
	}
	
	Map<String,String> getMap(String name) {
		return getMap(getNode(name));
	}
	
	private Map<String,String> getMapOfXml(Node node) {
		ImmutableMap.Builder<String,String> builder = new ImmutableMap.Builder<String,String>();
		for (Node item : new ItemIterable(node)) {
			String key = item.getAttributes().getNamedItem(KEY).getNodeValue();
			String value = getXml(item);
			builder.put(key, value);
		}
		return builder.build();
	}
	
	private Map<String,String> getMapOfXmlReferences(Node node) {
		ImmutableMap.Builder<String,String> builder = new ImmutableMap.Builder<String,String>();
		for (Node item : new ItemIterable(node)) {
			String key = item.getAttributes().getNamedItem(KEY).getNodeValue();
			String filename = getString(item);
			String value;
			try {
				value = FileUtils.readFileToString(new File(filename), Charsets.UTF_8.toString());
			} catch (IOException e) {
				throw new Error(e);
			}
			builder.put(key, value);
		}
		return builder.build();
	}
	
	Map<String,String> getMapOfXml(String name) {
		return getMapOfXml(getNode(name));
	}
	
	Map<String,String> getMapOfXmlReferences(String name) {
		return getMapOfXmlReferences(getNode(name));
	}
	
	/* Nested */
	
	/**
	 * @param name	configuration element to get data from
	 * @return 		an immutable, ordered map.
	 */
	Map<String,List<String>> getMapOfLists(String name) {
		ImmutableMap.Builder<String,List<String>> builder = new ImmutableMap.Builder<String,List<String>>();
		for (Node item : new ItemIterable(name)) {
			String key = item.getAttributes().getNamedItem(KEY).getNodeValue();
			List<String> value = getList(item);
			builder.put(key, value);
		}
		return builder.build();
	}
	
	/**
	 * @param name	configuration element to get data from
	 * @return 		an immutable, ordered map.
	 */
	Map<String,Map<String,String>> getMapOfMaps(String name) {
		ImmutableMap.Builder<String,Map<String,String>> builder = new ImmutableMap.Builder<String,Map<String,String>>();
		for (Node item : new ItemIterable(name)) {
			String key = item.getAttributes().getNamedItem(KEY).getNodeValue();
			Map<String,String> value = getMap(item);
			builder.put(key, value);
		}
		return builder.build();
	}
	
	Map<String,Map<String,String>> getTransposedMapOfMaps(String name) {
		Set<String> itemKeys = new HashSet<String>();
		Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
		for (Node item : new ItemIterable(name)) {
			String itemKey = item.getAttributes().getNamedItem(KEY).getNodeValue();
			if (itemKeys.contains(itemKey)) {
				throw new IllegalArgumentException("Key '"+itemKey+"' should be used only once in '"+name+"'");
			}
			itemKeys.add(itemKey);
			for (Node attr : new AttributeIterable(item)) {
				String mapKey = attr.getNodeName();
				String value = attr.getTextContent();
				if (!map.containsKey(mapKey)) {
					map.put(mapKey, new LinkedHashMap<String,String>());
				}
				map.get(mapKey).put(itemKey, value);
			}
		}
		return ImmutableMap.copyOf(map); // TODO: but the inside map isn't immutable.
	}
	
	/* Iterators */
	
	protected class ItemIterable implements Iterable<Node> {
		private Node node;
		
		public ItemIterable(String name) {
			this(getNode(name));
		}
		
		public ItemIterable(Node node) {
			this.node = node;
		}
		
		@Override
		public Iterator<Node> iterator() {
			return new ItemIterator(node);
		}
	}
	
	private class ItemIterator implements Iterator<Node> {
		private final NodeList children;
		private final int length;
		private int i = 0;
		private Node next;
		
		public ItemIterator(Node node) {
			children = node.getChildNodes();
			length = children != null 
					? children.getLength()
					: 0;
			fetchNext();
		}
		
		private void fetchNext() {
			next = null;
			while (i < length) {
				Node child = children.item(i);
				i++;
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					if (ITEM.equals(child.getNodeName())) {
						next = child;
						break;
					} else {
						throw new UnexpectedElementException("Unexpected element '"+child.getNodeName()+"'");
					}
				} else if (child.getNodeType() == Node.TEXT_NODE && !"".equals(child.getTextContent().trim())) {
					throw new UnexpectedTextException("Unexpected text '"+child.getTextContent()+"'");
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Node next() {
			Node current = next;
			fetchNext();
			return current;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private class AttributeIterable implements Iterable<Node> {
		private Node node;
		
		public AttributeIterable(Node node) {
			this.node = node;
		}
		
		@Override
		public Iterator<Node> iterator() {
			return new AttributeIterator(node);
		}
	}
	
	private class AttributeIterator implements Iterator<Node> {
		private final NamedNodeMap attributes;
		private final int length;
		private int i = 0;
		private Node next;
		
		public AttributeIterator(Node node) {
			attributes = node.getAttributes();
			length = attributes != null 
					? attributes.getLength()
					: 0;
			fetchNext();
		}
		
		private void fetchNext() {
			next = null;
			while (i < length) {
				Node child = attributes.item(i);
				i++;
				if (child.getNodeType() == Node.ATTRIBUTE_NODE) {
					next = child;
					break;
				} else {
					throw new Error("There should only be attribute children");
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Node next() {
			Node current = next;
			fetchNext();
			return current;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/* Exceptions */
	
	@SuppressWarnings("serial")
	public static class RedundantElementException extends Error {
		public RedundantElementException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public static class UnexpectedElementException extends Error {
		public UnexpectedElementException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public static class UnexpectedTextException extends Error {
		public UnexpectedTextException(String message) {
			super(message);
		}
	}

}
