package org.ssatguru.vaadin.watson;

import com.vaadin.server.StreamResource.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.vaadin.server.StreamResource;

public class ImageStreamSource implements StreamResource.StreamSource {

	ByteArrayOutputStream bao;

	public ImageStreamSource(ByteArrayOutputStream bao) {
		this.bao = bao;

	}

	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(bao.toByteArray());
	}

}
