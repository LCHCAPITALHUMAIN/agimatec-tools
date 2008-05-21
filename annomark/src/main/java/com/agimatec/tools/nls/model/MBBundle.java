package com.agimatec.tools.nls.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:50 <br/>
 * Copyright: Agimatec GmbH
 */
@XStreamAlias("bundle")
public class MBBundle {
    @XStreamAsAttribute
    private String baseName;
    @XStreamAsAttribute
    private String interfaceName;
    @XStreamAsAttribute
    private String sqldomain;
    @XStreamImplicit
    private List<MBEntry> entries = new ArrayList();

    public List<MBEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MBEntry> entries) {
        this.entries = entries;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getSqldomain() {
        return sqldomain;
    }

    public void setSqldomain(String sqldomain) {
        this.sqldomain = sqldomain;
    }

}
