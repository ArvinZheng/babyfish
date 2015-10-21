package org.babyfishdemo.war3shop.db.installer.schema;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Date;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Tao Chen
 */
public interface ParsingContext {

	File getBaseDir();
	
	long getMillis();
	
	Map<String, Object> getVariables();
	
	Date parseDate(String text) throws ParseException;
	
	void loadText(String relativePath, Writer writer);
	
	void loadImage(String relativePath, OutputStream outputStream);
}
