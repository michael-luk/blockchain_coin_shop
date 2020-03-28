	jQuery(document).ready(function() { 
	
		$(".dropdown").mouseover(function () {
			$(this).addClass("open");
		});
	
		$(".dropdown").mouseleave(function(){
			$(this).removeClass("open");
		})
	
	
		App.initBxSlider();
		App.handleSearch();
	});
