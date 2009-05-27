package com.agimatec.annotations.jam;

import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 12:10:31 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class JAMDtoAnnotatedElement {
    private final Map<String, JAMAnnotation> annotations = new HashMap();

    public abstract String getName();

    public abstract String getType();

    public abstract JClass getTypeJClass();

    public abstract JAMDtoClass getDtoClass();

    public abstract boolean isEnumType();

    public JAMAnnotation getAnnotation(String anno) {
        JAMAnnotation ja = annotations.get(anno);
        if (ja == null) {
            JAnnotation a = element().getAnnotation(anno);
            if (a != null) {
                ja = new JAMAnnotation(a);
                annotations.put(anno, ja);
            }
        }
        return ja;
    }

    public abstract JAnnotatedElement element();

    protected String stringValue(String v, String defaultValue) {
        return v == null || v.length() == 0 ? defaultValue : v;
    }

    public String getComment() {
        if (element().getComment() == null) return null;
        String c = element().getComment().getText();
        return c == null || c.length() == 0 ? null : c;
    }

    protected boolean isCurrentlyActive(JAMAnnotation ja) {
        String e = ja.getStringValue("usage");
        if (e == null || e.length() == 0) return true;
        String ce = JAMDtoGenerator.getCurrentEntity();
        return ce == null || ce.length() == 0 || e.equals(ce);
    }

    /**
     * the first active dto annotation or null
     * @return
     */
    public JAMAnnotation getDtoAnnotation() {
        JAMAnnotation ja = getAnnotation(singleAnnotation());
        if (ja == null) {
            ja = getAnnotation(multiAnnotation());
            if (ja != null) {
                for (JAMAnnotation each : ja.getAnnotationArray()) {
                    if (isCurrentlyActive(each)) {
                        return each;
                    }
                }
            }
            return null;
        } else {
            return (isCurrentlyActive(ja)) ? ja : null;
        }
    }

    /**
     * all active dto annotations or null
     * @return
     */
    public JAMAnnotation[] getDtoAnnotations() {
        JAMAnnotation ja = getAnnotation(singleAnnotation());
        if (ja == null) {
            ja = getAnnotation(multiAnnotation());
            if (ja != null) {
                List<JAMAnnotation> activeAnnotations = new ArrayList(ja.getAnnotationArray().length);
                for (JAMAnnotation each : ja.getAnnotationArray()) {
                    if (isCurrentlyActive(each)) {
                        activeAnnotations.add(each);
                    }
                }
                return activeAnnotations.toArray(new JAMAnnotation[activeAnnotations.size()]);
            }
            return null;
        } else {
            return (isCurrentlyActive(ja)) ? new JAMAnnotation[]{ja} : null;
        }
    }

    protected abstract String singleAnnotation();
    protected abstract String multiAnnotation();

    public boolean isDtoCurrentlyActive() {
        return getDtoAnnotation() != null;
    }

    public String toString() {
        return element().toString();
    }

    public abstract JField getTypeField(String dtoPath);

    protected static JField findField(JField root, String each) {
        return findField(root.getType(), each);
    }
    
    protected static JField findField(JClass rootCls, String each) {
        JField[] fields = rootCls.getFields();
        for (JField field : fields) {
            if (field.getSimpleName().equals(each)) return field;
        }
        return null;
    }

    public String getGenericParameter() {
        throw new UnsupportedOperationException("not yet implemented for this type");
    }

}
