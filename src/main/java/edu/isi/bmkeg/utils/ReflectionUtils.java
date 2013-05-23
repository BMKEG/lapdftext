package edu.isi.bmkeg.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.TreeMap;
/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class ReflectionUtils {
	
	/**
	 * Perform an unchecked cast based on a type parameter.
	 * 
	 * @param <T> The type to which the object should be cast.
	 * @param o   The object.
	 * @return    The object, cast to the given type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object o) {
		return (T)o;
	}
	
	public static interface TypeArgumentDelegator {
		public Map<String,Type> getTypeArguments(Class<?> genericType);
	}
	
	public static <T> Type getTypeArgument(Class<T> genericType, String typeParameterName, T obj) {
		Map<String,Type> typeArguments = getTypeArguments(genericType, obj);
		return typeArguments == null ? null : typeArguments.get(typeParameterName);
	}
	
	/**
	 * Try to find the instantiation of all of genericTypes type parameters in objs class.
	 * 
	 * @param genericType	the generic supertype of objs class
	 * @param obj			an instantiation of a subclass of genericType. All of genericTypes type
	 * 						parameters must have been instantiated in the inheritance hierarchy.
	 * @return				a map of genericTypes type parameters (their name in the source code) to
	 * 						the type they are instantiated as in obj
	 */
	public static Map<String,Type> getTypeArguments(Class<?> genericType, Object obj) {
		if (obj instanceof TypeArgumentDelegator) {
			return ((TypeArgumentDelegator)obj).getTypeArguments(genericType);
		}
		Map<String,Type> typeMap = new TreeMap<String,Type>();
		return getTypeArguments(genericType, obj.getClass(), typeMap);
	}
	
	public static boolean isAssignableFrom(Type type1, Type type2) {
		if (type1 instanceof Class<?> && type2 instanceof Class<?>) {
			return ((Class<?>)type1).isAssignableFrom((Class<?>)type2);
		} else {
			return type1.equals(type2);
		}
	}

	private static Map<String,Type> getTypeArguments(Class<?> genericType, Type type, Map<String,Type> typeMap) {
		if( type instanceof ParameterizedType ) {
			return getTypeArguments(genericType, (ParameterizedType)type, typeMap);
		} else 	if( type instanceof Class<?> ) {
			return getTypeArguments(genericType, (Class<?>) type, typeMap);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Map<String,Type> getTypeArguments(Class<?> genericType, Class<?> classType, Map<String,Type> typeMap) {
		if( genericType.isInterface() ) {
			for( Type interfaceType : classType.getGenericInterfaces() ) {
				Map<String,Type> result = getTypeArguments(genericType, interfaceType, typeMap);
				if( result != null )
					return result;
			}
		}

		Type superType = classType.getGenericSuperclass();
		if( superType != null ) {
			return getTypeArguments(genericType, superType, typeMap);
		}

		return null;
	}
	
	private static Map<String,Type> getTypeArguments(Class<?> genericType, ParameterizedType paramType, Map<String,Type> typeMap) {
		Class<?> rawType = (Class<?>) paramType.getRawType();
		if( rawType == genericType ) {
			// found it!
			TypeVariable<?> typeVars[] = rawType.getTypeParameters();
			Type actualTypes[] = paramType.getActualTypeArguments();
			Map<String,Type> result = new TreeMap<String,Type>();
			for( int i=0; i<actualTypes.length; i++ ) {
				while( actualTypes[i] != null && actualTypes[i] instanceof TypeVariable<?> ) {
					String key = typevarString((TypeVariable<?>) actualTypes[i]);
					if( typeMap.containsKey(key) )
						actualTypes[i] = typeMap.get(key);
					else
						actualTypes[i] = null;
				}
				result.put(typeVars[i].getName(), actualTypes[i]);
			}
			return result;
		} else {
			TypeVariable<?> typeVars[] = rawType.getTypeParameters();
			Type actualTypes[] = paramType.getActualTypeArguments();
			for( int i=0; i<typeVars.length; i++ )
				typeMap.put(typevarString(typeVars[i]), actualTypes[i]);
			return getTypeArguments(genericType, paramType.getRawType(), typeMap);
		}
	}
	
	private static String typevarString(TypeVariable<?> tv) {
		return tv.getGenericDeclaration().toString() + " " + tv.getName();
	}

}
