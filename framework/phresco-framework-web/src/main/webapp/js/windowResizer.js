// JavaScript Document
$(document).ready(function() {
	$(window).bind("resize", resizeWindow);
	var heightDiff_MainWrpr = $(window).height()- $(".main_wrapper").height();
	var heightDiff_Wrpr = $(".main_wrapper").height()- $(".wrapper").height();
	var heightDiff_Content = $(".wrapper").height()- $("#container").height();
	var heightDiff_tablediv = $("#container").height()- $(".table_div").height();
	var heightDiff_tabledatadiv = $(".table_div").height()- $(".table_data_div").height();
	
	

	function resizeWindow(e) {
		var newHeight_Window = $(window).height();
		$(".main_wrapper").css("height",newHeight_Window - heightDiff_MainWrpr);
		
		var newHeight_MainWrpr = $(".main_wrapper").height();
		$(".wrapper").css("height",newHeight_MainWrpr - heightDiff_Wrpr);
		
		var newHeight_content = $(".wrapper").height();
		$("#container").css("height",newHeight_content - heightDiff_Content);
		
		var newHeight_addinfodiv = $("#container").height();
		$(".appInfoTabDiv").css("height",newHeight_addinfodiv - 66);
		
		/* Appinfo page */
		var newHeight_addformdiv = $(".appInfoTabDiv").height();
		$(".app_add_form").css("height",newHeight_addformdiv - 15);
		
		var newHeight_appinfo_div = $(".app_add_form").height();
		$(".appInfoScrollDiv").css("height", newHeight_appinfo_div - 35) ;
		
		/* Feature page */
		var newHeight_feature_div = $(".appInfoTabDiv").height();
		$(".app_features_form").css("height",newHeight_feature_div - 10);
		
		var newHeight_feature_div = $(".app_features_form").height();
		$(".featuresScrollDiv").css("height", newHeight_feature_div - 75);
		
		/* Configuration add page */
		var newHeight_config_div = $(".appInfoTabDiv").height();
		$(".configurations_add_form").css("height",newHeight_config_div - 12);
		
		var newHeight_config_div = $(".configurations_add_form").height();
		$(".config_div").css("height", newHeight_config_div - 55);
		
		/* Settings page */
		var newHeight_settingdiv = $("#container").height();
		$(".settings_add_form").css("height",newHeight_settingdiv - 40);
		
		var newHeight_sttngs_div = $(".settings_add_form").height();
		$(".settings_div").css("height", newHeight_sttngs_div - 30);
		
		var newHeight_tabledatadiv = $(".table_div").height();
		$(".table_data_div").css("height",newHeight_tabledatadiv - heightDiff_tabledatadiv);

		var newHeight_tablediv = $("#container").height();
		$(".table_div").css("height",newHeight_tablediv - heightDiff_tablediv);
		
		var newHeight_tabledatadiv = $(".table_div").height();
		$(".table_data_div").css("height",newHeight_tabledatadiv - heightDiff_tabledatadiv);
	}
});