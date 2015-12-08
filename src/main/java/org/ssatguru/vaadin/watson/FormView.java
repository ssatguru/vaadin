package org.ssatguru.vaadin.watson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ssatguru.watson.visualrecognition.VisualRecognitionService;
import org.ssatguru.watson.visualrecognition.response.ImageData;
import org.ssatguru.watson.visualrecognition.response.Label;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingErrorEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;

import elemental.json.JsonArray;

public class FormView extends FormDark implements Upload.Receiver, Upload.StartedListener, Upload.SucceededListener,
Upload.FailedListener, DropHandler, MouseEvents.ClickListener {

	
	final String THUMBNAIL_PANEL_WIDTH ="815px";
	//final String THUMBNAIL_PANEL_WIDTH ="100%";
	final String THUMBNAIL_PANEL_HEIGHT ="100px";
	final String IMAGE_PANEL_WIDTH = "500px";
	final String IMAGE_PANEL_HEIGHT = "500px";
	final String UPLOAD_BUTTON_WIDTH = "815px";
	final String RESULT_PANEL_WIDTH = "300px";
	final String RESULT_PANEL_HEIGHT = "500px";
	
	VisualRecognitionService service;

	HorizontalLayout thumbnailLayout = new HorizontalLayout();
	com.vaadin.ui.Image selectedImage;

	com.vaadin.ui.Image uploadImage = new com.vaadin.ui.Image();

	Panel imageHolder = new Panel(".");
	
	
	Upload uploadButton;

	Panel analysisRsltPanel = new Panel("Analysis result");
	VerticalLayout resultsLayout = new VerticalLayout();

	MTable<LabelFormatted> results = new MTable<>(LabelFormatted.class);

	// a byte array output stream for the uploader to write bytes being uploaded
	ByteArrayOutputStream bout;
	byte[] imageByteArray = null;

	String imageName;

	VerticalLayout analysisProgressLayout = new VerticalLayout();
	VerticalLayout uploadProgressLayout = new VerticalLayout();

	boolean uploadOn = false;
	boolean analysisOn = false;

	public FormView() {

		super();

		// image thumbnails
		Panel thumbnailPanel = new Panel("Uploaded images");
		thumbnailPanel.setContent(thumbnailLayout);
		thumbnailLayout.setMargin(true);
		thumbnailLayout.setHeight(THUMBNAIL_PANEL_HEIGHT);
		thumbnailPanel.setWidth(THUMBNAIL_PANEL_WIDTH);
		thumbnailLayout.setSpacing(true);

		// results
		analysisRsltPanel.setWidth(RESULT_PANEL_WIDTH );
		analysisRsltPanel.setHeight(RESULT_PANEL_HEIGHT);

		resultsLayout.setMargin(true);
		resultsLayout.addComponent(results);
		results.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
		results.setWidth("100%");

		ProgressBar uploadProgressBar = new ProgressBar();
		uploadProgressBar.setIndeterminate(true);
		uploadProgressLayout.setSizeFull();
		uploadProgressLayout.addComponent(uploadProgressBar);
		uploadProgressLayout.setComponentAlignment(uploadProgressBar, Alignment.MIDDLE_CENTER);

		ProgressBar analysisProgressBar = new ProgressBar();
		analysisProgressBar.setIndeterminate(true);
		analysisProgressLayout.setSizeFull();
		analysisProgressLayout.addComponent(analysisProgressBar);
		analysisProgressLayout.setComponentAlignment(analysisProgressBar, Alignment.MIDDLE_CENTER);

		//final DragAndDropWrapper dropBox = new DragAndDropWrapper(imageHolder);
		//dropBox.setDropHandler(this);

		// this is to allow uploaded image to show on page before calling watson
		// service
		// otherwise image is not shown until watson call is finished
		JavaScript.getCurrent().addFunction("com.ssatguru.invokeService1", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				JavaScript.getCurrent().execute("com.ssatguru.invokeService2()");
			}
		});

		// this is to call watson service after uploaded image has been shown on
		// page
		JavaScript.getCurrent().addFunction("com.ssatguru.invokeService2", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				ImageData imageData = service.recognize(imageByteArray);
				List<LabelFormatted> labels = formatLabels(imageData);
				results.setBeans(labels);
				results.setPageLength(results.size());
				analysisRsltPanel.setContent(resultsLayout);
				selectedImage.setData(labels);
				uploadButton.setEnabled(true);
				imageHolder.setCaption(formatImageCaption(labels.get(0)));
				analysisOn = false;
			}
		});

		service = new VisualRecognitionService();

		uploadButton = new Upload(null, this);
		// uploadButton.setIcon(FontAwesome.FILES_O);
		uploadButton.setButtonCaption("Click here to upload an image file or drag and drop images anywhere in the UI to upload");
		uploadButton.setImmediate(true);
		uploadButton.setWidth(UPLOAD_BUTTON_WIDTH);
		uploadButton.addStartedListener(this);
		uploadButton.addSucceededListener(this);

		imageHolder.setWidth(IMAGE_PANEL_WIDTH);
		imageHolder.setHeight(IMAGE_PANEL_HEIGHT);

		/*
		VerticalLayout uploadComponents = new VerticalLayout();
		uploadComponents.setSpacing(true);
		uploadComponents.addComponent(imageHolder);
		uploadComponents.addComponent(uploadButton);
		*/
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		//hl.addComponent(uploadComponents);
		hl.addComponent(imageHolder);
		hl.addComponent(analysisRsltPanel);
		
	
		VerticalLayout mainVertLayout = new VerticalLayout();
		mainVertLayout.setSizeFull();
		mainVertLayout.setSizeUndefined();
		mainVertLayout.setSpacing(true);
		mainVertLayout.addComponent(uploadButton);
		mainVertLayout.addComponent(thumbnailPanel);
		mainVertLayout.addComponent(hl);

		this.main_content_wrapper.addComponent(mainVertLayout);
	
		
		this.main_content_wrapper.setSizeUndefined();
		this.setSizeUndefined();

	}
	
	private List<LabelFormatted> formatLabels(ImageData imgData){
		List< Label> lbls = imgData.getLabels();
		ArrayList<LabelFormatted> lblsFormatted = new<LabelFormatted> ArrayList();
		LabelFormatted lblFormatted;
		for(Label lbl:lbls){
			lblFormatted = new LabelFormatted();
			lblFormatted.setLableName(lbl.getLabelName());
			int score = (int) (lbl.getLabelScore()*100);
			lblFormatted.setLableScore(Integer.toString(score) + " %");
			lblsFormatted.add(lblFormatted);
		}
		return lblsFormatted;
	}

	// implementation of Upload.Receiver
	// gives uploader a stream to write to
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		imageName = filename;
		bout = new ByteArrayOutputStream();
		return bout;
	}

	@Override
	public void uploadStarted(StartedEvent event) {

		if (!event.getMIMEType().startsWith("image/")) {
			Notification.show("Invalid file type. Only image file please");
			uploadButton.interruptUpload();
			return;
		}
		imageName = event.getFilename();
		imageHolder.setCaption(".");
		imageHolder.setContent(uploadProgressLayout);
		analysisRsltPanel.setContent(analysisProgressLayout);
		uploadButton.setEnabled(false);
		uploadOn = true;
	}
	
	public void resetUpload(){
		uploadButton.setEnabled(true);
		imageHolder.setContent(new VerticalLayout());
		analysisRsltPanel.setContent(new VerticalLayout());
		uploadOn = false;
	}
	
	@Override
	public void uploadFailed(FailedEvent event) {
		resetUpload();
		
	}

	// implementation of Upload.SucceededListener
	@Override
	public void uploadSucceeded(SucceededEvent event) {
		// add a timestamp to prevent caching
		long time = new Date().getTime();
		StreamResource resource = new StreamResource(new ImageStreamSource(bout), imageName + "-" + time);
		
		//add to thumbnail
		uploadImage = new com.vaadin.ui.Image(null, resource);
		uploadImage.setHeight("100%");
		uploadImage.addClickListener(this);
		thumbnailLayout.addComponentAsFirst(uploadImage);
		selectedImage = uploadImage;

		//add to image pane (just a link to above)
		String uri = getResourceURL(uploadImage, resource);
		//com.vaadin.ui.Image img2 = new com.vaadin.ui.Image(null, resource);
		com.vaadin.ui.Image panelImg =  new com.vaadin.ui.Image(null, new ExternalResource(uri));
		panelImg.setWidth("100%");
		imageHolder.setContent(panelImg);
		
		imageByteArray = bout.toByteArray();
		// return to client and then call watson service so that uploaded images
		// are displayed before watson is called
		JavaScript.getCurrent().execute("com.ssatguru.invokeService1()");
		uploadOn = false;
		analysisOn = true;
	}

	String lastFile;
	private static final long FILE_SIZE_LIMIT = 2 * 1024 * 1024; // 2MB
	// implementation of DropHandler

	@Override
	public void drop(final DragAndDropEvent dropEvent) {
		if (analysisOn || uploadOn) {
			Notification.show("Upload or analysis in progress");
			return;
		}

		// expecting this to be an html5 drag
		final WrapperTransferable tr = (WrapperTransferable) dropEvent.getTransferable();
		final Html5File[] files = tr.getFiles();
		for (final Html5File html5File : files) {
			if (html5File.getType().startsWith("image/")) {
				lastFile = html5File.getFileName();
			}
		}
		if (files != null) {
			for (final Html5File html5File : files) {
				final String fileName = html5File.getFileName();

				if (html5File.getFileSize() > FILE_SIZE_LIMIT) {
					Notification.show("File " + fileName + " rejected. Max 2Mb files are accepted by Sampler",Notification.Type.WARNING_MESSAGE);
				}else if (!html5File.getType().startsWith("image/")) {
					Notification.show("File " + fileName + " rejected. Not image", Notification.Type.WARNING_MESSAGE);
				} else {
					final ByteArrayOutputStream bas = new ByteArrayOutputStream();
					final StreamVariable streamVariable = new StreamVariable() {

						@Override
						public OutputStream getOutputStream() {
							return bas;
						}

						@Override
						public boolean listenProgress() {
							return false;
						}

						@Override
						public void onProgress(final StreamingProgressEvent event) {
						}

						@Override
						public void streamingStarted(final StreamingStartEvent event) {
							imageHolder.setCaption(".");
							imageHolder.setContent(uploadProgressLayout);
							analysisRsltPanel.setContent(analysisProgressLayout);
							uploadButton.setEnabled(false);
							uploadOn = true;
						}

						@Override
						public void streamingFinished(final StreamingEndEvent event) {
							com.vaadin.ui.Image img = getFile(fileName, html5File.getType(), bas);
							img.setHeight("100%");
							img.addClickListener(FormView.this);
							thumbnailLayout.addComponentAsFirst(img);

							if (fileName.equals(lastFile)) {
								//add to image pane (just a link to above)
								String uri = getResourceURL(img, (StreamResource) img.getSource());
								//com.vaadin.ui.Image img2 = new com.vaadin.ui.Image(null, resource);
								com.vaadin.ui.Image panelImg =  new com.vaadin.ui.Image(null, new ExternalResource(uri));
								panelImg.setWidth("100%");
								imageHolder.setContent(panelImg);
								
								selectedImage = img;
								JavaScript.getCurrent().execute("com.ssatguru.invokeService1()");
								imageByteArray = bas.toByteArray();
								uploadOn = false;
								analysisOn = true;
							}

						}

						@Override
						public void streamingFailed(final StreamingErrorEvent event) {
							uploadProgressLayout.setVisible(false);
						}

						@Override
						public boolean isInterrupted() {
							return false;
						}
					};
					html5File.setStreamVariable(streamVariable);

				}
			}

		} else {
			final String text = tr.getText();
			if (text != null) {
				// showText(text);
			}
		}
	}

	// implementation of DropHandler
	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	private com.vaadin.ui.Image getFile(final String name, final String type, final ByteArrayOutputStream bas) {
		// resource for serving the file contents
		final StreamSource streamSource = new StreamSource() {
			@Override
			public InputStream getStream() {
				if (bas != null) {
					final byte[] byteArray = bas.toByteArray();
					return new ByteArrayInputStream(byteArray);
				}
				return null;
			}
		};
		// add a timestamp to prevent caching
		long time = new Date().getTime();
		final StreamResource resource = new StreamResource(streamSource, name + "-" + time);

		// show the file contents - images only for now
		final com.vaadin.ui.Image image = new com.vaadin.ui.Image(null, resource);
		return image;
	}

	@Override
	public void click(ClickEvent event) {
		if (analysisOn || uploadOn) {
			return;
		}
		com.vaadin.ui.Image imgClicked = (com.vaadin.ui.Image) event.getComponent();
		
		String uri = getResourceURL(imgClicked,(StreamResource) imgClicked.getSource());
		com.vaadin.ui.Image panelImg = new com.vaadin.ui.Image(null, new ExternalResource(uri));
		panelImg.setWidth("100%");
		imageHolder.setContent(panelImg);
		selectedImage = imgClicked;
		
		if (imgClicked.getData() != null) {
			List<LabelFormatted> labels = (List<LabelFormatted>) imgClicked.getData();
			results.setBeans(labels);
			results.setPageLength(results.size());
			analysisRsltPanel.setContent(resultsLayout);
			imageHolder.setCaption(formatImageCaption(labels.get(0)));
		} else {
			imageHolder.setCaption(".");
			analysisRsltPanel.setContent(analysisProgressLayout);
			StreamResource resource = (StreamResource) imgClicked.getSource();
			ByteArrayInputStream bis = (ByteArrayInputStream) resource.getStreamSource().getStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while (true) {
				int i = bis.available();
				if (i == 0)
					i = 1;
				byte[] bytes = new byte[i];
				try {
					int r = bis.read(bytes);
					if (r == -1)
						break;
					bos.write(bytes);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			imageByteArray = bos.toByteArray();
			uploadButton.setEnabled(false);
			JavaScript.getCurrent().execute("com.ssatguru.invokeService1()");
			analysisOn = true;
		}
	}
	
	private String formatImageCaption(LabelFormatted label){
		return (label.getLableName() + " ( " + label.getLableScore() + " )");
	}

	String getResourceURL(AbstractClientConnector connector,StreamResource resource){

		  String protocol = UI.getCurrent().getPage().getLocation().getScheme();
		  String currentUrl = UI.getCurrent().getPage().getLocation().getAuthority();
		  String cid = connector.getConnectorId();
		  Integer uiId = connector.getUI().getUIId();
		  String filename = resource.getFilename();

		  return protocol+"://"+currentUrl+"/APP/connector/"+uiId+"/"+cid+"/source/"+filename;
		}
	
	private void printData2(ImageData imgData) {
		List<Label> lbls = imgData.getLabels();
		System.out.println(lbls.size());
		for (Label lbl : lbls) {
			System.out.println(lbl.getLabelName());
		}

	}

	

}
