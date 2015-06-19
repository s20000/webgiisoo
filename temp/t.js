var $ul=$(this).closest("ul[selectiontype=2]");var minVoteQuantity=parseInt($ul.attr("minVoteQuantity"));
                var maxVoteQuantity=parseInt($ul.attr("maxVoteQuantity"));
                var currentSelectedCount=$ul.find("li input:checkbox:checked").length;

                if (maxVoteQuantity!=-1 && currentSelectedCount>maxVoteQuantity) {
                    $(this).prop("checked",false);
                    alert("ok " + maxVoteQuantity.toString());
                    $.uniform.update($(this));
                    return;
                }

                var message="";
                
                if (currentSelectedCount>0 && (minVoteQuantity>1 || maxVoteQuantity!=-1)) {
                    message += "selected " + currentSelectedCount.toString();
                }
                if (minVoteQuantity>1 && currentSelectedCount<minVoteQuantity) {
                    if (message!="") {  message += "；"; }
                    message += "at least " + (minVoteQuantity-currentSelectedCount).toString();
                }
                if (maxVoteQuantity!=-1) {
                    if (currentSelectedCount<maxVoteQuantity) {
                        if (message!="") {  message += "；"; }
                        message += "max " + (maxVoteQuantity-currentSelectedCount).toString();
                    }
                    else if (currentSelectedCount==maxVoteQuantity) {
                        if (message!="") {  message += "；"; }
                        message += "maxed";
                    }
                }
                if (message!="") {
                    $(".CheckboxSelectedCountMessage").show();
                    $(".CheckboxSelectedCountMessage span").text(message);
                }

