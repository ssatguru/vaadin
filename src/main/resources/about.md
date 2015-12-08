This App is the Submission for the final  challenge, called *"Services in Bluemix"*, in the Vaadin Challenge Competition by IBM

The App  analyzes images, uploaded to it, to understand and describe the content of those images.
The analysis is done using [IBM Bluemix Watson Visual Recognition Service] (https://console.ng.bluemix.net/catalog/services/visual-recognition/) 

The App UI was developed using Vaadin Designer and Vaadin IBM Design Language UI designer template.

The following briefly describes few salient features of the the App.

* user can upload a single image , using the upload button or , if the browser is capable of drag and drop, can also upload multiple images at the same time by dragging and dropping those images anywhere into the UI.

* on upload, the UI is immediately updated to display the image and then and only after that, a call is made to the Bluemix Waston Visual Recognition service. The service call is done as a call back from the client side, using Vaadin Javascript Function API. This improves perceived response time.

* all uploaded images, along with their  results, are stored in the App for quick review. 
These images are displayed as thumbnails in a thumbnail panel (the latest image is displayed first).
Clicking a thumbnail displays the image along with its analysis result. 

* when multiple images are uploaded, using drag and drop, they are stored in the thumbnail panel, with the last image also displayed in the main image panel. The last image is the only  one sent for analysis. The other images are analyzed as and when the user clicks on them. This, kind of "lazy" analysis, again, improves perceived response time.

*Satguru P Srivastava*
  
 
