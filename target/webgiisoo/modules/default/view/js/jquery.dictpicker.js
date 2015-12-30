(function($) {
	$.fn.dictpicker = function(getdata) {
		/**
		 * set the dict picker dialog
		 */
		var b = $('#dict-picker');
		if (b.length == 0) {
			b = $("<div id='dict-picker' class='picker'></div>");
			$('body').append(b);
		}

		b.click(function(e) {
					e.stopPropagation();
				});

		$('body').click(function() {
					b.hide();
				});

		/**
		 * set the attribute back to the node
		 */
		var nodes = this;
		nodes.attr('readonly', 'readonly');
		nodes.addClass('dictpicker-node');
		nodes.each(function(i, e) {
					var e = $(e);
					e.css('color', 'rgba(0,0,0,0)');
					var mark = $("<span class='mark'></span>");
					mark.css('left', e.position().left + 'px');
					mark.css('top', e.position().top + 'px');
					mark.css('height', e.height() + 'px');
					mark.css('width', (e.width() - 10) + 'px');
					mark.css('line-height', e.height() + 'px');
					e.parent().append(mark);
				});
		nodes.attr('href', '#dict-picker');
		nodes.leanModal();
		nodes.click(function(e) {

			var that = $(this);
			b.html('<img src="/images/loading2.gif"/>');

			var multiple = that.prop('multiple');
			if (typeof(getdata) == 'function') {
				getdata(that, function(d) {
					var s = '';
					if ((!d) || d.length == 0) {
						s = '没有数据项...';
					} else {
						$(d).each(function(i, e) {
							if (multiple) {
								s += "<div class='item' value='"
										+ e.value
										+ "'><label><input type='checkbox' value='"
										+ e.value + "'/><span>" + e.name
										+ "</span></label></div>";
							} else {
								s += "<div class='item' value='" + e.value
										+ "'><span>" + e.name + "</span></div>";
							}
						});

						s = "<div class='picker_class'>" + s + "</div>";
						if (multiple) {
							s += '<div class="btns"><a class="ok" href="javascript:;">确定</a><a class="clear" href="javascript:;">清空</a></div>';
						} else {
							s += '<div class="btns"><a class="clear" href="javascript:;">清空</a></div>';
						}
					}
					b.html(s);
					b.find('.picker_class').jScrollPane();

					/**
					 * hook the mouse move on the item
					 */
					var ss = b.find('.item');
					ss.bind('mouseenter', function() {
								$(this).addClass('hover');
							});
					ss.bind('mouseleave', function() {
								$(this).removeClass('hover');
							});

					/**
					 * set the click event on the item
					 */
					if (multiple) {
						var s = that.val();
						$(s.split(',')).each(function(i, e) {
									var ii = b.find('input[value="' + e + '"]');
									if (ii.length > 0) {
										ii[0].checked = true;
									}
								});
					} else {
						var s = that.val();
						b.find('.item[value="' + s + '"]').addClass('selected');
						ss.click(function() {
									that.attr('value', $(this).attr('value'));
									var title = $(this).find('span').text();
									var tt = that.parent().find('span.mark');
									tt.html('<span>' + title + '</span>');
									if (!tt.hasClass('hint')) {
										tt.addClass('hint hint--bottom');
									}
									tt.attr('data-hint', title);

									that.trigger('blur');

									$('#lean_overlay').click();
								});
					}

					b.find('.btns a').click(function() {
						if ($(this).attr('class') == 'ok') {
							var s = '';
							var title = '';
							b.find('input:checked').each(function(i, e) {
										if (s.length > 0) {
											s += ',';
											title += ',';
										}
										s += e.value;
										title += $(e).parent().find('span')
												.text();
									});

							that.attr('value', s);
							var tt = that.parent().find('span.mark');
							tt.html('<span>' + title + '</span>');
							if (!tt.hasClass('hint')) {
								tt.addClass('hint hint--bottom');
							}
							tt.attr('data-hint', title);

							$('#lean_overlay').click();

							that.trigger('blur');

						} else {
							/**
							 * clean
							 */
							that.attr('value', '');

							var tt = that.parent().find('span.mark');
							tt.html('');
							tt.removeClass('hint hint--bottom');
							tt.removeAttr('data-hint');

							$('#lean_overlay').click();
						}
					});
				});
			}

			e.stopPropagation();
		});

		/**
		 * load the original value and name
		 */
		setTimeout(function() {
			nodes.each(function(i, e) {
				if (e.value != '') {
					var that = $(e);
					var ss = e.value.split(',');
					if (typeof(getdata) == 'function') {
						getdata(that, function(d) {
									var title = '';
									$(d).each(function(i, e1) {
												$(ss).each(function(i, e2) {
															if (e2 == e1.value) {
																if (title.length > 0)
																	title += ',';
																title += e1.name;
															}
														});
											})
									var tt = that.parent().find('span.mark');
									tt.html('<span>' + title + '</span>');
									if (!tt.hasClass('hint')) {
										tt.addClass('hint hint--bottom');
									}
									tt.attr('data-hint', title);
								}, e.value)
					}
				}
			})
		}, 100);

	};
})(jQuery);
