	/***********************/
	/* Check ODS file page */
	/***********************/

	$("#choose-file-button").click(function (e) {
		$("#input-file-name").click();
	});	

	function showODSErrorMsg(msg) {
    	if ($("#error-summary").length) {
    		$("#error-summary").remove();
    		$(".validation-summary").hide()
	    	$("#uploadForm").removeClass("error");
    	}
    	$("#file-uploader button").attr("disabled",true);
    	$(".validation-summary").show()
    	$("#file-wrapper").addClass("fileAlert");
	    $("#file-uploader").before("<p id='error-summary' class='field-error clear' tabindex'-1' role='alert' aria-labelledby='error-heading'>"+msg+"</p>")
	    $(".validation-summary-message a").html(msg)
	    $("#uploadForm").addClass("error");
	    $("#errors").focus();
	}
	
	
	$("#fileToUpload").change(function(e){					
		var $el = $('#fileToUpload');		
		// extract file name for validation 
		if (ie<10) {			
			var fileName = $el.val().substr($el.val().lastIndexOf("\\")+1, $el.val().length);	
		} else {
			var fileName = $("#fileToUpload")[0].files[0].name;
		}		
		
		// Check file name
		if (validFileName(fileName)) {			
			// check file name length
			if (fileName.length <= MAX_FILENAME_LENGTH) {
				// Check file extn
				if (getFileNameExtension(fileName) == "ods") {
					if (fileSizeOK()) {
						// file ok
				    	removeErrorMsg();						
					} else {
						showODSErrorMsg("This file is larger than "+(MAX_FILESIZE/1000000)+"MB &ndash; choose a different file or email <a href='mailto:shareschemes@hmrc.gsi.gov.uk'>shareschemes@hmrc.gsi.gov.uk</a> and we’ll help you submit your return");
					}
				} else {
			    	showODSErrorMsg("This file isn’t a .ods file, choose a different file");
				}				
			} else {
		    	showODSErrorMsg("The filename must contain "+ MAX_FILENAME_LENGTH +" characters or less");
		    }
		} else {
	    	showODSErrorMsg("The filename contains invalid characters");
		}		
				
		// extract filename for display 
		$("#file-name").text(fileName);	
		
		// show page elements
		$("#file-header-bar").show();
		
		if (ie<11) {
			$("#remove-file-link").insertAfter("#input-file-name")
			$("#file-header-bar").css("padding-left","3px")
		}		
		$("#remove-file-link").show();
	});
	
	
	
	    