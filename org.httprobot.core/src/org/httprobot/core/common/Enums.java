/**
 * 
 */
package org.httprobot.core.common;

/**
 * Common core enumerations.
 * @author joan
 *
 */
public class Enums 
{
	public enum UnitData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		ACTION,
		CONSTANT,
		BANNED_WORD,
		WEB_OPTIONS,
		PAGINATOR,
		STRICT_MODE,
		HTTP_ADDRESS,
		METHOD,
		NEXT_PAGE_ANCHOR_HREF,
		NEXT_PAGE_ANCHOR_VALUE,
		PAGINATOR_INCREMENT,
		PAGINATOR_URL_PATTERN,
		ACTIVE_X_NATIVE_ENABLED,
		APPLET_ENABLED,
		BROWSER_VERSION,
		CSS_ENABLED,
		GEO_LOCATION_ENABLED,
		JAVA_SCRIPT_ENABLED,
		PAGINATOR_ENABLED,
		PERIOD_TIME,
		POPUP_BLOCKER_ENABLED,
		REDIRECT_MODE,
		TIMEOUT,
		TYPE,
		USE_PROXY_ENABLED
	}
	public enum ConfigData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		Q_NAME,
		URL,
		SYSTEM_CONTENT_TYPE_ROOT,
		DATA_SOURCE,
		CONFIGURATION,
		LOG,
		SESSION,
		SERVICE_CONNECTION
	}
	public enum HtmlData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		CONTAINS,
		DELIMITERS,
		END_INDEX,
		EQUALS,
		REMOVE,
		REPLACE,
		SPLIT,
		START_INDEX,
		SUBSTRING,
		TRY_PARSE,
		ID,
		TITLE,
		CLASS,
		STYLE,
		HREF,
		TEXT_CONTENT,
		NAME,
		CHARSET,
		HREF_LANG,
		TYPE,
		TARGET,
		ANCHOR,
		DIVISION,
		ELEMENT,
		PAGE,
		TABLE_CELL,
		TABLE_ROW,
		TABLE,
		X_PATH,
		NODE_NAME,
		TAG_NAME
	}
	public enum OperatorData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		CONTAINS,
		DELIMITERS,
		END_INDEX,
		EQUALS,
		REMOVE,
		REPLACE,
		SPLIT,
		START_INDEX,
		SUBSTRING,
		TRY_PARSE,
		VALUE,
		OLD_STRING,
		NEW_STRING,
		FIELD_TYPE
	}
	public enum ContentData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		INHERITED_TYPE,
		CONTENT_TYPE_ROOT,
		CONTENT_TYPE,
		CONTENT_TYPE_REF,
		DATA_VIEW,
		FIELD_REF,
		COMPRESSED,
		COMPRESS_THRESHOLD,
		DATA_TYPE,
		INDEXED,
		MULTI_VALUED,
		NAME,
		OMIT_NORM,
		OMIT_POSITIONS,
		OMIT_TERM_FREQ_AND_POSITIONS,
		STORED,
		TERM_VECTORS,
		TYPE
	}
	public enum ParameterData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		PARAMETER_NAME,
		PARAMETER_TYPE,
		VALUE,
		SERVER_URL,
		START_URL,
		CONSTANT,
		BANNED_WORD
	}
	public enum DataTypeData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		NTP,
		SOURCE_NAME,
		FIELD_NAME,
		FIELD_TYPE,
		DATA_SOURCE,
		ACTION,
		CONTENT_TYPE_REF,
		DOCUMENT_ROOT,
		DOCUMENT,
		SERVER_URL,
		START_URL,
		FIELD_ROOT,
		FIELD,
		HTTP_ADDRESS,
		HTML_UNIT
	}
	public enum PlaceholderData
	{
		UUID,
		INHERITED,
		RUNTIME_OPTIONS,
		HTML_UNIT,
		HTTP_ADDRESS,
		FIELD_REF,
		CONTAINS,
		EQUALS,
		REMOVE,
		REPLACE,
		SPLIT,
		SUBSTRING,
		TRY_PARSE,
		PAGE
	}
}