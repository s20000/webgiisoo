/**
 * 
 */
var menuapi = menuapi | false;
var panelapi = panelapi | false;
var uploaddone = uploaddone | false;
var editor = editor | false;
var __history = [];

var processing = {
	show : function() {
		$('#processing').show();
	},
	hide : function() {
		$('#processing').hide();
	}
};

$(function() {
			$(window).resize(function() {
						resize();
					});

			var e = $('#ss_tab');
			e.bind('mouseenter', function() {
						var e1 = $(this);
						if (e1.hasClass('info_but_close')) {
							e1.addClass('info_but_close_on');
						} else {
							e1.addClass('info_but_open_on');
						}
					});

			e.bind('mouseleave', function() {
						var e1 = $(this);
						e1.removeClass('info_but_close_on');
						e1.removeClass('info_but_open_on');
					});

			e.click(function() {
						var bar = $(this);
						var menu = $('#menu');
						var panel = $('#content');
						var toolbar = $('#toolbar');

						if (bar.hasClass('info_but_close')) {
							bar.addClass('info_but_open');
							bar.removeClass('info_but_close');
							bar.removeClass('info_but_close_on');

							panel.addClass('full');
							toolbar.addClass('full');
							menu.hide();

							resize();
						} else {
							bar.addClass('info_but_close');
							bar.removeClass('info_but_open');
							bar.removeClass('info_but_open_on');

							panel.removeClass('full');
							toolbar.removeClass('full');

							menu.show();

							resize();
						}
					});

		});

function history(url) {
	if (url && url.length > 0) {
		if (__history[__history.length - 1] !== url) {
			__history.push(url);
		}
	}
}

function back() {
	if (__history.length > 1) {

		var h = __history.pop();
		h = __history.pop();

		load(h);
	}
}

function verify(obj, url) {
	var that = $(obj);
	$.post(url, {
				name : that.attr('name'),
				value : that.val()
			}, function(d) {
				if (d.state == 200) {
					/**
					 * ok, good
					 */
					that.attr('bad', "0");
					if (d.value) {
						that.val(d.value);
					}
					that.removeClass('bad').addClass('good');
					that.parent().parent().find('.note').hide();
				} else if (d.state == 400) {
					/**
					 * need confirm
					 */
					if (confirm(d.message)) {
						that.attr('bad', "0");
						if (d.value) {
							that.val(d.value);
						}
						that.removeClass('bad').addClass('good');
						that.parent().parent().find('.note').hide();
					} else {
						that.attr('bad', "1");
						that.removeClass('good').addClass('bad');
						var note = that.parent().parent().find('.note');
						if (note.length == 0) {
							note = $('<div class="note"></div>');
							that.parent().parent().append(note);
						}
						note.html(d.error).show();
					}
				} else {
					that.attr('bad', "1");
					that.removeClass('good').addClass('bad');
					var note = that.parent().parent().find('.note');
					if (note.length == 0) {
						note = $('<div class="note"></div>');
						that.parent().parent().append(note);
					}
					note.html(d.message).show();
				}
			});
}

function show(html) {

	uploaddone = false;
	editor = false;
	try {
		$('#panel .content').html(html);
	} catch (e) {
		console.log(e);
	}

	hook();

	// resize();

}

function hook() {
	/**
	 * hook all the <a> tag
	 */
	$('#panel .content a').each(function(i, e) {
		e = $(e);
		var href = e.attr('href');
		var target = e.attr('target');
		if (target == undefined && href != undefined
				&& (href.indexOf('javascript') == -1)
				&& (href.indexOf('#') != 0)) {

			e.click(function(e1) {
						var href = $(this).attr('href');
						if (href != undefined) {
							load(href);
						}

						// console.log(href);
						e1.preventDefault();
					});
		}
	});

	/**
	 * hook all <form> to smooth submit
	 */
	$('#panel form').submit(function(e) {
		e.preventDefault();

		var form = e.target;

		var beforesubmit = $(form).attr('beforesubmit');
		if (typeof window[beforesubmit] === 'function') {
			if (!window[beforesubmit](form)) {
				return;
			}
		}

		/**
		 * check the bad flag
		 */
		var bad = $(form).find("input[bad=1], textarea[bad=1], select[bad=1]");
		if (bad.length > 0) {
			$.error("[" + $(bad[0]).parent().find('h3').text()
					+ "]数据输入有错误，请更正！");
			bad[0].focus();
			return;
		}
		var bb = $(form).find("input[required=true], select[required=true]");
		for (i = 0; i < bb.length; i++) {
			var e = $(bb[i]);
			if (e.val() == '') {
				$.error("[" + e.parent().find('h3').text() + "]必须输入数据，请更正！");
				e.focus();
				return;
			}
		}

		var url = form.action;

		if (form != undefined && url != undefined) {

			processing && processing.show();

			if (form.method == 'get') {
				var data = $(form).serialize();
				if (editor && editor.srcElement && editor.srcElement.length > 0) {
					var src = editor.srcElement[0];
					data += "&" + src.name + "=" + editor.html();
				}

				var __url = '';
				if (url.indexOf('?') > 0) {
					__url = url + '&' + data;
				} else {
					__url = url + '?' + data;
				}
				if (__history.length > 0
						&& __history[__history.length - 1] == __url) {
					__history.pop();
				}
				__history.push(__url);

				if (url.indexOf('?') > 0) {
					url += '&' + new Date().getTime();
				} else {
					url += '?' + new Date().getTime();
				}

				$.get(url, {}, function(d) {
							show(d);
							processing && processing.hide();
						});

			} else {
				var data = new FormData(form);
				if (editor && editor.srcElement && editor.srcElement.length > 0) {
					var src = editor.srcElement[0];
					data.append(src.name, editor.html());
				}

				var xhr = new XMLHttpRequest();
				xhr.open("POST", url);
				xhr.overrideMimeType("multipart/form-data");
				xhr.send(data);

				xhr.onreadystatechange = function() {
					if (xhr.readyState == 4) {
						if (xhr.status == 200) {
							show(xhr.responseText);
							processing && processing.hide();
						}
					}
				}

			}
		}

	});

	/**
	 * hook all <textarea> with "richedit=true" flag
	 */
	editor = false;
	editor = KindEditor.create('#panel textarea[richedit=true]', {
				basePath : '/ke/',
				resizeType : 1,
				allowPreviewEmoticons : false,
				allowImageUpload : true,
				items : ['fontname', 'fontsize', '|', 'forecolor',
						'hilitecolor', 'bold', 'italic', 'underline',
						'removeformat', '|', 'justifyleft', 'justifycenter',
						'justifyright', 'insertorderedlist',
						'insertunorderedlist', '|', 'emoticons', 'image',
						'link']
			});

	/**
	 * hook the <checkbox> on table header
	 */
	$('#panel table th.checkbox').click(function(e) {
				var ch = $(this).find('input[type=checkbox]');
				if (ch.length > 0) {
					var en = ch[0].checked;
					var t = $(this);
					while (t.length > 0 && t[0].nodeName !== 'TABLE') {
						t = t.parent();
					}
					t.find('td input[type=checkbox]').each(function(i, e) {
								if (!e.disabled) {
									e.checked = en;
								}
							});
				}
			});

	var options = options || {};

	/**
	 * hook all <select> associated group
	 */
	$('#panel select[parentnode=true]').change(function(e) {
		var ch = $(this);
		if (ch.length > 0) {
			var value = ch.val();
			var subnode = ch.attr('subnode');
			var n1 = $('select[name=' + subnode + ']');
			/**
			 * initialize the options
			 */
			if (options[ch.attr('name')]) {
				n1.html(options[ch.attr('name')]);
			} else {
				options[ch.attr('name')] = n1.find('option');
			}

			var valid = false;
			var best = undefined;
			n1.find('option').each(function(i, e) {
				e = $(e);
				if (e.attr('parent') == value || e.attr('parent') == undefined) {
					e.show();
					if (best === undefined) {
						best = e.val();
					}
					if (!valid && e.val() == n1.val()) {
						valid = true;
					}
				} else {
					// e.hide();
					e.remove();
				}
			});

			if (!valid) {
				n1.val(best);
				n1.trigger('change');
			}
		}
	});

	/**
	 * hook all <select> to make
	 */
	$('#panel select').each(function(i, e) {
				if (e.value == '') {
					$(e).removeClass('setted');
				} else {
					$(e).addClass('setted');
				}
			}).change(function(e) {
				if (this.value == '') {
					$(this).removeClass('setted');
				} else {
					$(this).addClass('setted');
				}
			});

	/**
	 * hook tr.hover
	 */
	$('#panel table.tablesorter tr').bind('mouseenter', function() {
				$(this).addClass('hover');
			}).bind('mouseleave', function() {
				$(this).removeClass('hover');
			});

	/**
	 * hook td.hover
	 */
	$('#panel table.tablesorter td').bind('mouseenter', function() {
				$(this).addClass('hover');
			}).bind('mouseleave', function() {
				$(this).removeClass('hover');
			});

	/**
	 * setting all searchbar
	 */
	$('#panel div.search').searchbar();

	$('#panel input').bind('focus', function() {
				$(this).parent().addClass('focus');
			}).bind('blur', function() {
				var that = $(this);
				that.parent().removeClass('focus');
				if (that.attr('verify')) {
					// verify
				}
				if (that.attr('max')) {
					// check max
					var value = that.val();
					console.log(value + ", " + value.length + ", "
							+ that.attr('max'));
				}
			});

	/**
	 * setting all date-picker
	 */
	$.beatpicker();

}

function reload() {
	if (__history.length > 1) {
		load(__history.pop());
		return true;
	}

	return false;
}

function load(uri) {
	processing && processing.show();

	if (__history.length > 0 && __history[__history.length - 1] == uri) {
		__history.pop();
	}
	__history.push(uri);

	// $('#page').attr('src', uri);
	if (uri.indexOf('?') > 0) {
		uri += '&' + new Date().getTime();
	} else {
		uri += '?' + new Date().getTime();
	}
	$.ajax({
				url : uri,
				type : 'GET',
				data : {},
				error : function(d) {
					processing && processing.hide();
					$.error('访问错误，请重新登录');
				},
				success : function(d, status, xhr) {
					processing && processing.hide();
					show(d);
				}
			})
}

function load1(uri) {
	processing && processing.show();

	// $('#page').attr('src', uri);
	if (uri.indexOf('?') > 0) {
		uri += '&' + new Date().getTime();
	} else {
		uri += '?' + new Date().getTime();
	}
	var s = '<iframe src="' + uri + '"></iframe>';
	processing && processing.hide();
	show(s);
}

function resize() {
	var menu = $('#menu');
	if (menu.length > 0) {
		var h = $(window).height();
		menu.css('height', (h - 120) + 'px');

		if (!menuapi) {
			menuapi = menu.jScrollPane().data('jsp');
		} else {
			menuapi.reinitialise();
		}
	}
}

function _post(o, table, max) {
	var s = '';
	var name = '';
	var selected = $(table + ' td input:checked');
	if (selected.length == 0) {
		$.error('您没有选择任何数据，请重选择数据！');
		return;
	}

	if (max != undefined && max == -1) {
		if (!confirm('请确认要删除选中的数据项。\r<确定>立刻删除\r<取消>重新选择')) {
			return;
		}
	}

	if (max != undefined && max != -1 && selected.length > max) {
		if (!confirm('您选择了多条数据。\r<确定>只对第一条数据操作\r<取消>重新选择')) {
			return;
		}
	}

	selected.each(function(i, e) {
				if (s.length > 0)
					s += ',';
				s += e.value;
				name = e.name;
			});

	var p = {};
	p[name] = s;

	if (o.indexOf('?') > 0) {
		o += '&' + new Date().getTime();
	} else {
		o += '?' + new Date().getTime();
	}
	$.get(o, p, function(d) {

				show(d);
			});
}
