jQuery.extend({
	message : function(message, onclick) {
		$('#error.leanmodal').hide();
		var m = $('#message.leanmodal');
		if (m.length == 0) {
			m = $("<div id='message' class='leanmodal'><div class='leanmodal-header'></div><div class='leanmodal-content'></div></div>");
			$('body').append(m);
		}
		m.find('.leanmodal-content').html(message);
		var overlay = $('#lean_overlay');
		if (overlay.length == 0) {
			overlay = $("<div id='lean_overlay'></div>");
			$("body").append(overlay);
		}
		overlay.css({
					'display' : 'block',
					opacity : 0.5
				});
		m.css({
					'display' : 'block'
				});
		overlay.click(function() {
					overlay.hide();
					$('.leanmodal').hide();
					
					onclick && onclick();
				});
		m.click(function() {
					overlay.click();
				});
	},
	error : function(message, onclick) {
		$('#message.leanmodal').hide();
		var m = $('#error.leanmodal');
		if (m.length == 0) {
			m = $("<div id='error' class='leanmodal'><div class='leanmodal-header'></div><div class='leanmodal-content'></div></div>");
			$('body').append(m);
		}
		m.find('.leanmodal-content').html(message);
		var overlay = $('#lean_overlay');
		if (overlay.length == 0) {
			overlay = $("<div id='lean_overlay'></div>");
			$("body").append(overlay);
		}
		overlay.css({
					'display' : 'block',
					opacity : 0.5
				});
		m.css({
					'display' : 'block'
				});
		overlay.click(function() {
					overlay.hide();
					$('.leanmodal').hide();

					onclick && onclick();
				});
		m.click(function() {
					overlay.click();
				});
	},
	confirm : function(title, message) {
	}
});
