package org.xson.tangyuan.type;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class TypeHandlerRegistry {

	private static final Map<Class<?>, Class<?>>			reversePrimitiveMap		= new HashMap<Class<?>, Class<?>>() {
																						private static final long	serialVersionUID	= 1L;

																						{
																							put(Byte.class, byte.class);
																							put(Short.class, short.class);
																							put(Integer.class, int.class);
																							put(Long.class, long.class);
																							put(Float.class, float.class);
																							put(Double.class, double.class);
																							put(Boolean.class, boolean.class);
																							put(Character.class, char.class);
																						}
																					};

	/**
	 * key: JdbcType, value: 处理器
	 */
	private final Map<JdbcType, TypeHandler<?>>				JDBC_TYPE_HANDLER_MAP	= new EnumMap<JdbcType, TypeHandler<?>>(JdbcType.class);
	/**
	 * key: javaType,
	 */
	private final Map<Type, Map<JdbcType, TypeHandler<?>>>	TYPE_HANDLER_MAP		= new HashMap<Type, Map<JdbcType, TypeHandler<?>>>();
	private final TypeHandler<Object>						UNKNOWN_TYPE_HANDLER	= new UnknownTypeHandler(this);

	// private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<Class<?>, TypeHandler<?>>();

	public TypeHandlerRegistry() {
	}

	public void init(Map<String, TypeHandler<?>> jdbcTypeMap) {
		BooleanTypeHandler booleanTypeHandler = BooleanTypeHandler.instance;
		ByteTypeHandler byteTypeHandler = ByteTypeHandler.instance;
		ShortTypeHandler shortTypeHandler = ShortTypeHandler.instance;
		IntegerTypeHandler integerTypeHandler = IntegerTypeHandler.instance;
		LongTypeHandler longTypeHandler = LongTypeHandler.instance;
		FloatTypeHandler floatTypeHandler = FloatTypeHandler.instance;
		DoubleTypeHandler doubleTypeHandler = DoubleTypeHandler.instance;
		StringTypeHandler stringTypeHandler = StringTypeHandler.instance;
		ClobTypeHandler clobTypeHandler = ClobTypeHandler.instance;
		NStringTypeHandler nStringTypeHandler = NStringTypeHandler.instance;
		NClobTypeHandler nClobTypeHandler = NClobTypeHandler.instance;
		// BigIntegerTypeHandler bigIntegerTypeHandler = BigIntegerTypeHandler.instance;
		BigDecimalTypeHandler bigDecimalTypeHandler = BigDecimalTypeHandler.instance;

		DateTypeHandler dateTypeHandler = DateTypeHandler.instance;
		DateOnlyTypeHandler dateOnlyTypeHandler = DateOnlyTypeHandler.instance;
		TimeOnlyTypeHandler timeOnlyTypeHandler = TimeOnlyTypeHandler.instance;

		SqlDateTypeHandler sqlDateTypeHandler = SqlDateTypeHandler.instance;
		SqlTimeTypeHandler sqlTimeTypeHandler = SqlTimeTypeHandler.instance;
		SqlTimestampTypeHandler sqlTimestampTypeHandler = SqlTimestampTypeHandler.instance;

		CharacterTypeHandler characterTypeHandler = CharacterTypeHandler.instance;
		ArrayTypeHandler arrayTypeHandler = ArrayTypeHandler.instance;
		ByteObjectArrayTypeHandler byteObjectArrayTypeHandler = ByteObjectArrayTypeHandler.instance;
		ByteArrayTypeHandler byteArrayTypeHandler = ByteArrayTypeHandler.instance;
		BlobTypeHandler blobTypeHandler = BlobTypeHandler.instance;
		BlobByteObjectArrayTypeHandler blobByteObjectArrayTypeHandler = BlobByteObjectArrayTypeHandler.instance;

		register(Boolean.class, booleanTypeHandler);
		register(boolean.class, booleanTypeHandler);
		register(JdbcType.BOOLEAN, booleanTypeHandler);
		register(JdbcType.BIT, booleanTypeHandler);

		register(Byte.class, byteTypeHandler);
		register(byte.class, byteTypeHandler);
		// register(JdbcType.TINYINT, byteTypeHandler);
		if (jdbcTypeMap.containsKey("TINYINT")) {
			register(JdbcType.TINYINT, jdbcTypeMap.get("TINYINT"));
		} else {
			register(JdbcType.TINYINT, byteTypeHandler);
		}

		register(Short.class, shortTypeHandler);
		register(short.class, shortTypeHandler);
		// register(JdbcType.SMALLINT, shortTypeHandler);
		if (jdbcTypeMap.containsKey("SMALLINT")) {
			register(JdbcType.SMALLINT, jdbcTypeMap.get("SMALLINT"));
		} else {
			register(JdbcType.SMALLINT, byteTypeHandler);
		}

		register(Integer.class, integerTypeHandler);
		register(int.class, integerTypeHandler);
		register(JdbcType.INTEGER, integerTypeHandler);

		register(Long.class, longTypeHandler);
		register(long.class, longTypeHandler);

		register(Float.class, floatTypeHandler);
		register(float.class, floatTypeHandler);
		register(JdbcType.FLOAT, floatTypeHandler);

		register(Double.class, doubleTypeHandler);
		register(double.class, doubleTypeHandler);
		register(JdbcType.DOUBLE, doubleTypeHandler);

		register(String.class, stringTypeHandler);
		register(String.class, JdbcType.CHAR, stringTypeHandler);
		register(String.class, JdbcType.CLOB, clobTypeHandler);
		register(String.class, JdbcType.VARCHAR, stringTypeHandler);
		register(String.class, JdbcType.LONGVARCHAR, clobTypeHandler);
		register(String.class, JdbcType.NVARCHAR, nStringTypeHandler);
		register(String.class, JdbcType.NCHAR, nStringTypeHandler);
		register(String.class, JdbcType.NCLOB, nClobTypeHandler);
		register(JdbcType.CHAR, stringTypeHandler);
		register(JdbcType.VARCHAR, stringTypeHandler);
		register(JdbcType.CLOB, clobTypeHandler);
		register(JdbcType.LONGVARCHAR, clobTypeHandler);
		register(JdbcType.NVARCHAR, nStringTypeHandler);
		register(JdbcType.NCHAR, nStringTypeHandler);
		register(JdbcType.NCLOB, nClobTypeHandler);

		register(Object.class, JdbcType.ARRAY, arrayTypeHandler);
		register(JdbcType.ARRAY, arrayTypeHandler);

		// register(BigInteger.class, bigIntegerTypeHandler);
		register(BigInteger.class, longTypeHandler); // TODO BUG
		register(JdbcType.BIGINT, longTypeHandler);

		register(BigDecimal.class, bigDecimalTypeHandler);
		// register(JdbcType.REAL, bigDecimalTypeHandler);
		// register(JdbcType.DECIMAL, bigDecimalTypeHandler);
		// register(JdbcType.NUMERIC, bigDecimalTypeHandler);
		if (jdbcTypeMap.containsKey("REAL")) {
			register(JdbcType.REAL, jdbcTypeMap.get("REAL"));
		} else {
			register(JdbcType.REAL, bigDecimalTypeHandler);
		}
		if (jdbcTypeMap.containsKey("DECIMAL")) {
			register(JdbcType.DECIMAL, jdbcTypeMap.get("DECIMAL"));
		} else {
			register(JdbcType.DECIMAL, bigDecimalTypeHandler);
		}
		if (jdbcTypeMap.containsKey("NUMERIC")) {
			register(JdbcType.NUMERIC, jdbcTypeMap.get("NUMERIC"));
		} else {
			register(JdbcType.NUMERIC, bigDecimalTypeHandler);
		}

		register(Byte[].class, byteObjectArrayTypeHandler);
		register(Byte[].class, JdbcType.BLOB, blobByteObjectArrayTypeHandler);
		register(Byte[].class, JdbcType.LONGVARBINARY, blobByteObjectArrayTypeHandler);
		register(byte[].class, byteArrayTypeHandler);
		register(byte[].class, JdbcType.BLOB, blobTypeHandler);
		register(byte[].class, JdbcType.LONGVARBINARY, blobTypeHandler);
		register(JdbcType.LONGVARBINARY, blobTypeHandler);
		register(JdbcType.BLOB, blobTypeHandler);

		register(Object.class, UNKNOWN_TYPE_HANDLER);
		register(Object.class, JdbcType.OTHER, UNKNOWN_TYPE_HANDLER);
		register(JdbcType.OTHER, UNKNOWN_TYPE_HANDLER);

		register(Date.class, dateTypeHandler);
		register(Date.class, JdbcType.DATE, dateOnlyTypeHandler);
		register(Date.class, JdbcType.TIME, timeOnlyTypeHandler);
		register(JdbcType.TIMESTAMP, dateTypeHandler);
		register(JdbcType.DATE, dateOnlyTypeHandler);
		register(JdbcType.TIME, timeOnlyTypeHandler);

		register(java.sql.Date.class, sqlDateTypeHandler);
		register(java.sql.Time.class, sqlTimeTypeHandler);
		register(java.sql.Timestamp.class, sqlTimestampTypeHandler);

		// issue #273
		register(Character.class, characterTypeHandler);
		register(char.class, characterTypeHandler);
	}

	public boolean hasTypeHandler(Class<?> javaType) {
		return hasTypeHandler(javaType, null);
	}

	public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
		return hasTypeHandler(javaTypeReference, null);
	}

	public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
		return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
	}

	public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
		return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
	}

	public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
		return getTypeHandler((Type) type, null);
	}

	public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
		return getTypeHandler(javaTypeReference, null);
	}

	public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
		return JDBC_TYPE_HANDLER_MAP.get(jdbcType);
	}

	public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
		return getTypeHandler((Type) type, jdbcType);
	}

	public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
		return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
		Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
		TypeHandler<?> handler = null;
		if (jdbcHandlerMap != null) {
			handler = jdbcHandlerMap.get(jdbcType);
			if (handler == null) {
				handler = jdbcHandlerMap.get(null);
			}
		}
		if (handler == null && type != null && type instanceof Class && Enum.class.isAssignableFrom((Class<?>) type)) {
			handler = new EnumTypeHandler((Class<?>) type);
		}
		// type drives generics here
		TypeHandler<T> returned = (TypeHandler<T>) handler;
		return returned;
	}

	public TypeHandler<Object> getUnknownTypeHandler() {
		return UNKNOWN_TYPE_HANDLER;
	}

	public void register(JdbcType jdbcType, TypeHandler<?> handler) {
		JDBC_TYPE_HANDLER_MAP.put(jdbcType, handler);
	}

	// java type + handler
	public <T> void register(Class<T> javaType, TypeHandler<? extends T> typeHandler) {
		register((Type) javaType, typeHandler);
	}

	private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
		register(javaType, null, typeHandler);
	}

	public <T> void register(TypeReference<T> javaTypeReference, TypeHandler<? extends T> handler) {
		register(javaTypeReference.getRawType(), handler);
	}

	// java type + jdbc type + handler
	public <T> void register(Class<T> type, JdbcType jdbcType, TypeHandler<? extends T> handler) {
		register((Type) type, jdbcType, handler);
	}

	private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
		if (javaType != null) {
			Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.get(javaType);
			if (map == null) {
				map = new HashMap<JdbcType, TypeHandler<?>>();
				TYPE_HANDLER_MAP.put(javaType, map);
			}
			map.put(jdbcType, handler);
			if (reversePrimitiveMap.containsKey(javaType)) {
				register(reversePrimitiveMap.get(javaType), jdbcType, handler);
			}
		}
		// ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
	}
}
