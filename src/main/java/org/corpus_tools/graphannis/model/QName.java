package org.corpus_tools.graphannis.model;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class QName {
	private final String ns;
	private final String name;

	public QName(String ns, String name) {
		this.ns = ns;
		this.name = name;
	}

	public String getNs() {
		return ns;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof QName) {
			QName other = (QName) obj;
			return Objects.equals(this.ns, other.ns) && Objects.equals(this.name, other.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.ns).append(this.name).toHashCode();
	}
}
