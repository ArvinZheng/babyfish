/**
 * @author Tao Chen(&#38472;&#28059;)
 */
var createErrorDialog = function(e) {
    return createTemplate(
            "AlertDialog", 
            { 
                title: "Error", 
                message: e.exceptionMessage || e.errors || "Ajax requrest failed" 
            }
    );
};
