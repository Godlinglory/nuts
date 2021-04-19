///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc]
// * Licensed under the Apache License, Version 2.0 (the "License"); you may 
// * not use this file except in compliance with the License. You may obtain a 
// * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software 
// * distributed under the License is distributed on an 
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
// * either express or implied. See the License for the specific language 
// * governing permissions and limitations under the License.
// * <br>
// * ====================================================================
//*/
//package net.thevpc.nuts.runtime.core.format.elem;
//
//import net.thevpc.nuts.*;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.time.Instant;
//import java.util.Date;
//import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
//
//
///**
// *
// * @author thevpc
// */
//public class DefaultNutsPrimitiveElementBuilder implements NutsPrimitiveElementBuilder {
//
//    public static final NutsPrimitiveElement NULL = new DefaultNutsPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false);
//
//    private NutsSession session;
//    public DefaultNutsPrimitiveElementBuilder(NutsSession session) {
//        this.session=session;
//        if(session==null){
//            throw new NullPointerException();
//        }
//    }
//
//    @Override
//    public NutsPrimitiveElement buildNull() {
//        return NULL;
//    }
//
//    @Override
//    public NutsPrimitiveElement buildString(String value) {
//        if (value == null) {
//            return buildNull();
//        }
//        return new DefaultNutsPrimitiveElement(NutsElementType.STRING, value);
//    }
//
//
////    @Override
////    public NutsPrimitiveElement buildNutsString(NutsString value) {
////        if (value == null) {
////            return buildNull();
////        }
////        return new DefaultNutsPrimitiveElement(NutsElementType.NUTS_STRING, value);
////    }
//
//    @Override
//    public NutsPrimitiveElement buildNumber(Number value) {
//        if (value == null) {
//            return buildNull();
//        }
//        switch (value.getClass().getName()) {
//            case "java.lang.Byte":
//                return new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value);
//            case "java.lang.Short":
//                return new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value);
//            case "java.lang.Integer":
//                return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
//            case "java.lang.Long":
//                return new DefaultNutsPrimitiveElement(NutsElementType.LONG, value);
//            case "java.math.BigInteger":
//                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_INTEGER, value);
//            case "java.lang.float":
//                return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
//            case "java.lang.Double":
//                return new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value);
//            case "java.math.BigDecimal":
//                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_DECIMAL, value);
//        }
//        // ???
//        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildInt(int value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildLong(long value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildByte(byte value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildDouble(double value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildFloat(float value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildChar(char value) {
//        return new DefaultNutsPrimitiveElement(NutsElementType.STRING, String.valueOf(value));
//    }
//
//
//    @Override
//    public NutsPrimitiveElement buildNumber(String value) {
//        if (value == null) {
//            return buildNull();
//        }
//        if (value.indexOf('.') >= 0) {
//            try {
//                return buildNumber(Double.parseDouble(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return buildNumber(new BigDecimal(value));
//            } catch (Exception ex) {
//
//            }
//        } else {
//            try {
//                return buildNumber(Integer.parseInt(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return buildNumber(Long.parseLong(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return buildNumber(new BigInteger(value));
//            } catch (Exception ex) {
//
//            }
//        }
//        throw new NutsParseException(session,"unable to parse number " + value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildTrue() {
//        return TRUE;
//    }
//
//    @Override
//    public NutsPrimitiveElement buildFalse() {
//        return TRUE;
//    }
//
//    @Override
//    public NutsPrimitiveElement buildBoolean(boolean value) {
//        return value ? TRUE : FALSE;
//    }
//
//    @Override
//    public NutsPrimitiveElement buildBoolean(String value) {
//        return CoreBooleanUtils.parseBoolean(value, false, false) ? TRUE : FALSE;
//    }
//
//    @Override
//    public NutsPrimitiveElement buildInstant(Date value) {
//        if (value == null) {
//            return buildNull();
//        }
//        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, value.toInstant());
//    }
//
//    @Override
//    public NutsPrimitiveElement buildInstant(Instant value) {
//        if (value == null) {
//            return buildNull();
//        }
//        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, value);
//    }
//
//    @Override
//    public NutsPrimitiveElement buildInstant(String value) {
//        if (value == null) {
//            return buildNull();
//        }
//        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, DefaultNutsPrimitiveElement.parseDate(value));
//    }
//
//   
//    
//}
