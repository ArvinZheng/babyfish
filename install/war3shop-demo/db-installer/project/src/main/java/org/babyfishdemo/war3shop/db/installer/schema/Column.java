package org.babyfishdemo.war3shop.db.installer.schema;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;

/**
 * @author Tao Chen
 */
public class Column {
	
	private Table table;
	
	private int index;

	private String name;
	
	private Type type;
	
	private int length;
	
	private boolean nullable;
	
	private String unresolvedReference;
	
	private Column referencedColumn;
	
	Column(Table table, int index, Attributes attributes) {
		this.table = table;
		this.index = index;
		this.name = attributes.getValue(XMLConstants.NULL_NS_URI, "name").toUpperCase();
		String type = attributes.getValue(XMLConstants.NULL_NS_URI, "type");
		this.nullable = type.endsWith("?");
		if (this.nullable) {
			type = type.substring(0, type.lastIndexOf('?'));
		}
		int lengthIndex = type.indexOf('[');
		if (lengthIndex != -1) {
			String length = type.substring(lengthIndex + 1);
			type = type.substring(0, lengthIndex);
			if (length.endsWith("]")) {
				length = length.substring(0, length.lastIndexOf(']'));
				this.length = Integer.parseInt(length);
			}
		}
		try {
			this.type = Type.of(type);
		} catch (IllegalArgumentException ex) {
			throw new SchemaException(
					"Failed to resolve the type of \""
					+ this.table.getName()
					+ "\".\""
					+ this.name
					+ "\"",
					ex);
		}
		String reference = attributes.getValue(XMLConstants.NULL_NS_URI, "reference");
		if (reference != null && !reference.isEmpty()) {
			this.unresolvedReference = reference.toUpperCase();
		}
	}
	
	public Table getTable() {
		return this.table;
	}

	public String getName() {
		return this.name;
	}
	
	public int getIndex() {
		return this.index;
	}

	public Type getType() {
		return this.type;
	}

	public int getLength() {
		return this.length;
	}

	public boolean isNullable() {
		return this.nullable;
	}
	
	public Column getReferencedColumn() {
		return this.referencedColumn;
	}

	String getUnresolvedReference() {
		return this.unresolvedReference;
	}
	
	void setReferencedColumn(Column referencedColumn) {
		this.referencedColumn = referencedColumn;
		this.unresolvedReference = null;
	}
}
