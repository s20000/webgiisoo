(function($) {

	$.fn.extend({

				leanModal : function(options) {

					var defaults = {
						top : 100,
						overlay : 0.5,
						closeButton : null
					}

					var overlay = $('#lean_overlay');
					if (overlay.length == 0) {
						overlay = $("<div id='lean_overlay'></div>");
						$("body").append(overlay);
					}

					options = $.extend(defaults, options);

					return this.each(function() {

								var o = options;

								$(this).click(function(e) {

									var that = $(this);
									var modal_id = that.attr("href");

									$("#lean_overlay").click(function() {
												close_modal(modal_id);
											});

									$(o.closeButton).click(function() {
												close_modal(modal_id);
											});

									var modal_height = $(modal_id)
											.outerHeight();
									var modal_width = $(modal_id).outerWidth();

									$('#lean_overlay').css({
												'display' : 'block'
											});

									$('#lean_overlay').show();

									var pp = that.offset();

									var left = pp.left;
									var top = pp.top;
									var body_width = $('body').width();
									var body_height = $('body').height();
									if (left + modal_width > body_width - 10) {
										left = body_width - modal_width - 40;
									}

									if (top + modal_height > body_height - 10) {
										top = body_height - modal_height - 40;
									}

									$(modal_id).css({
												'display' : 'block',
												'position' : 'absolute',
												'z-index' : 11000,
												'left' : parseInt(left) + 'px',
												'top' : parseInt(top) + 'px'
											});

									$(modal_id).show();

									e.preventDefault();

								});

							});

					function close_modal(modal_id) {

						$("#lean_overlay").hide();

						$(modal_id).css({
									'display' : 'none'
								});

					}

				}
			});

})(jQuery);