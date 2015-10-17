var initChapters = function (parentDictinoary, chapaterClassName, fontSizeStep) {
    if (!parentDictinoary) {
        parentDictinoary = $("#dictionary");
    }
    if (!chapaterClassName) {
        chapaterClassName = "chapter";
    }
    if (typeof(fontSizeStep) != "number") {
        fontSize = 4;
    } else if (fontSizeStep < 1) {
        fontSize = 1;
    }
    /* private */ var initOneLevel = function(depth, parentDOM, parentNo, parentDictinoary) {
        var index = 0;
        var ul = $("<ul></ul>").appendTo(parentDictinoary);
        $("fieldset.chapter", parentDOM).each(function () {
        	var legend = $(this).children("legend:first");
        	if (legend.find("a[visited]").length) {
        		return true;
        	}
            var no;
            if (parentNo) {
                no = parentNo + "." + ++index;
            } else {
                no = "" + ++index;
            }
            var html = no + " " + legend.html();
            legend
            .empty()
            .append(
        		$("<a></a>")
                .attr("name", no)
                .attr("visited", "true")
                .css("font-size", (30 - depth * 4) + "px")
                .css("font-family", "Arial Black")
                .html(html)
            );
            var dictionaryItem =
            	$("<a></a>")
                .attr("href", "#" + no)
                .css("font-size", (30 - depth * 4) + "px")
                .css("font-family", "Arial Black")
                .html(html);
            var chapterName = $("a[name]", dictionaryItem);
            if (chapterName.length) {
            	chapterName.each(function() {
            		$(this).replaceWith($(this).html());
            	});
            }
            $("<li></li>")
            .append(dictionaryItem)
            .appendTo(ul);
            initOneLevel(
            	depth + 1,
                this,
                no,
                ul
            );
        });
    };
    initOneLevel(0, document.body, null, parentDictinoary);
};
var initCodeBlocks = function() {
	/* private */ var removeNewLineOfSpeicalComment = function(text, begin, end) {
		if (begin.length == 0 || end.length == 0) {
			throw new Error("IllegalArgumentException");
		}
		var bi = text.indexOf(begin);
		if (bi == -1) {
			return text;
		}
		var ei = text.indexOf(end);
		if (ei == -1) {
			return text;
		}
		if (bi >= ei) {
			return text;
		}
		var head = text.substring(0, bi);
		var mid = text.substring(bi + begin.length, ei);
		var tail = text.substring(ei + end.length);
		mid = mid.split(/\r\n?|\n/).join("");
		text = head + begin + mid + end + removeNewLineOfSpeicalComment(tail, begin, end);
		return text;
	};
	$("pre[class*='lang-']").each(function() {
		var pre = $(this);
		var text =
			pre
			.text()
			.replace("<![CDATA[", "")
			.replace("]]>", "")
			.split("\t").join("    ")
			.trim();
		text = removeNewLineOfSpeicalComment(text, "<!--{", "}-->");
		text = removeNewLineOfSpeicalComment(text, "/*{", "}*/");
		pre.text(text);
		pre.addClass("prettyprint linenums");
	});
	$("pre[class*='lang-sql']").each(function() {
		var pre = $(this);
		/*
		 * I don't know why google-code-prettify does not the hight-light of 
		 * "left", "inner", "right", "join", "on", "order", "asc", "desc", "distinct", "between"
		 * I try to replace "fetch"(JPQL keyword, not SQL keyword) to be comment, but this problem is still existing
		 * so I have to do this. also I handle the JPQL keyword "fetch" 
		 */
		pre.text(
				pre
				.text()
				
				.split(" left ").join(" /*<<<left>>>*/ ")
				.split("\nleft ").join("\n/*<<<left>>>*/ ")
				
				.split(" inner ").join(" /*<<<inner>>>*/ ")
				.split("\ninner ").join("\n/*<<<inner>>>*/ ")
				
				.split(" right ").join(" /*<<<right>>>*/ ")
				.split("\nright ").join("\n/*<<<right>>>*/ ")
				
				.split(" join ").join(" /*<<<join>>>*/ ")
				
				.split(" fetch ").join(" /*<<<fetch>>>*/ ")
				
				.split(" on ").join(" /*<<<on>>>*/ ")
				
				.split(" order ").join(" /*<<<order>>>*/ ")
				.split("\norder ").join("\n/*<<<order>>>*/ ")
				
				.split(" asc ").join(" /*<<<asc>>>*/ ")
				.split(" asc\r").join(" /*<<<asc>>>*/\r")
				.split(" asc\n").join(" /*<<<asc>>>*/\n")
				.split(/ asc$/).join(" /*<<<asc>>>*/")
				.split(" asc,").join(" /*<<<asc>>>*/,")
				
				.split(" desc ").join(" /*<<<desc>>>*/ ")
				.split(" desc\r").join(" /*<<<desc>>>*/\r")
				.split(" desc\n").join(" /*<<<desc>>>*/\n")
				.split(/ desc$/).join(" /*<<<desc>>>*/")
				.split(" desc,").join(" /*<<<desc>>>*/,")
				
				.split(" distinct ").join(" /*<<<distinct>>>*/ ")
				.split("(distinct ").join("(/*<<<distinct>>>*/ ")
				
				.split(/\nbetween(\r\n?|\n)/).join("\n/*<<<between>>>*/\n")
				.split(/\nbetween /).join("\n/*<<<between>>>*/ ")
				.split(/ between(\r\n?|\n)/).join(" /*<<<between>>>*/\n")
				.split(" between ").join(" /*<<<between>>>*/ ")
				
				.split(" over(").join(" /*<<<over>>>*/(")				
				
				.split(/^create /).join("/*<<<create>>>*/ ")
				.split(" index ").join(" /*<<<index>>>*/ ")
		);
	});
	prettyPrint(function() {
		$("pre.prettyprinted span.com").each(function() {
			var text = $(this).text();
			var len = text.length;
			if (len > 10 && text.substring(0, 5) == "/*<<<" && text.substring(len - 5, len) == ">>>*/") {
				$(this).replaceWith("<span class='kwd'>" + text.substring(5, len - 5) + "</span>");
			} else {
				if (len > 9 && text.substring(0, 5) == "<!--{" && text.substring(len - 4, len) == "}-->") {
					text = text.substring(5, len - 4).trim();
				} else if (len > 6 && text.substring(0, 3) == "/*{" && text.substring(len - 3, len) == "}*/") {
					text = text.substring(3, len - 3).trim();
				} else {
					return true;
				}
				$(this).replaceWith(text);
			}
		});
		$("pre.prettyprinted span.atv").each(function() {
			var text = $(this).text();
			text = text.substring(1, text.length - 1);
			var len = text.length;
			if (len > 6 && text.substring(0, 3) == "{{{" && text.substring(len - 3, len) == "}}}") {
				text = text.substring(3, len - 3).trim();
				text = text.split("[[").join("<");
				text = text.split("]]").join(">");
				$(this).replaceWith('"' + text + '"');
			}
		});
		$("pre[class*='lang-'] a[href]").addClass("link-in-code");
		$("pre[class*='lang-'] a[name]").addClass("pln");
	});
};
var initGrids = function() {
	$("table.grid>tr:not(.grid-title)").each(function(index) {
		$(this).addClass(index % 2 == 0 ? "grid-item" : "grid-alternating-item");
	});
	$("table.grid>tbody:not(.grid-title)").each(function(index) {
		$(this).addClass(index % 2 == 0 ? "grid-item" : "grid-alternating-item");
	});
}
var initButtons = function() {
	$(".button").button();
}