/*
    Prototype for EJB3 Implementations of Catalog<#if (catalog.schemaName)??> '${catalog.schemaName}'</#if> with ${dbms}
    File generated by "${generator.templateName}"
*/

${ejb3schema.generate()}

<#list ejb3schema.ejb3classesCollection?sort as ejb3>
${generator.outputToFile("com/agimatec/nucleus/persistence/model/${ejb3.className}.java")}package com.agimatec.nucleus.persistence.model;

import com.agimatec.annotations.*;
import com.agimatec.nucleus.common.model.*;
import org.compass.annotations.*;
import javax.persistence.*;
import java.util.*;

/**
 * ${ejb3.className} - EJB3 implementation
 *
 * <#if (ejb3.table.comment)??>${ejb3.table.comment}</#if>
 */
@Entity
@Table(name = "${ejb3.table.tableName}")
<#if ejb3.table.tableName?upper_case?starts_with("CV_")>
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
<#else>
// @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
</#if><#list ejb3.multiUniqueConstraints as uniqueCons>
@UniqueConstraint(columnNames = {<#list uniqueCons as col>"${col}"<#if uniqueCons?last!=col>,</#if></#list>})
</#list>
public class ${ejb3.className} implements java.io.Serializable {
    // Fields
<#list ejb3.attributes as attribute>
    /**
     * <#if (attribute.column.comment)??>${attribute.column.comment}<#else>column ${attribute.column.columnName}</#if>
     **/<#if
    attribute.column.defaultValue??>
    
    @com.agimatec.annotations.Default("${attribute.column.defaultValue?replace("'","")}")</#if>
    private ${attribute.javaType} ${attribute.attributeName};
</#list>

    // Relationships
<#list ejb3.relationships?sort as rel>

    <#if (rel.foreignKey.comment)??>    /**
     * <#if (rel.foreignKey.comment)??>${rel.foreignKey.comment}</#if>
     **/</#if>
    private ${rel.javaType} ${rel.attributeName}<#if rel.toMany && !rel.oneToOne>= new ArrayList<${rel.targetType.className}>(0)</#if>;
</#list>

    // Constructors

    /**
     * default constructor
     */
    public ${ejb3.className}() {
    }

    // Property accessors

<#list ejb3.attributes as attribute>
<#if ejb3.table.isPrimaryKeyColumn(attribute.column)>
    @Id
    @GeneratedValue</#if><#if attribute.column.columnName="version">
    @Version</#if>
    @Column(name="${attribute.column.columnName}"<#if
    !(attribute.column.nullable)>, nullable=false</#if><#if
    ejb3.table.isUnique(attribute.column.columnName)>, unique=true</#if><#if
    attribute.column.precisionEnabled && (attribute.javaType="String" || attribute.javaType="java.lang.String" || attribute.enumType)>, length=${attribute.column.precision}</#if>)<#if
    attribute.enumType>

    @Enumerated(EnumType.STRING)</#if><#if attribute.column.typeName = 'TEXT' ||
        attribute.column.typeName = 'BYTEA'>
    
    @Basic(fetch=FetchType.LAZY)</#if>
    public ${attribute.javaType} ${attribute.getter}() {
        return ${attribute.attributeName};
    }

    public void ${attribute.setter}(${attribute.javaType} ${attribute.attributeName}) {
        this.${attribute.attributeName} = ${attribute.attributeName};
    }
</#list>

<#list ejb3.relationships?sort as rel>
    <#if rel.type="ManyToOne">
    <#if rel.oneToOne>
    @OneToOne(<#if !(rel.column.nullable)>optional = false</#if>)
    <#else>
    @ManyToOne(<#if !(rel.column.nullable)>optional = false</#if>)
    </#if><#if rel.primaryKeyJoin>
    @PrimaryKeyJoinColumn(name="${rel.column.columnName}")
    <#else>
    @JoinColumn(name="${rel.column.columnName}"<#if !(rel.column.nullable)>, nullable=false<#if
    ejb3.table.isUnique(rel.column.columnName)>, unique=true</#if></#if>)
    </#if><#if
        rel.foreignKey.onDeleteRule?? && rel.foreignKey.onDeleteRule?upper_case="CASCADE">
    @org.hibernate.annotations.OnDelete(action=OnDeleteAction.CASCADE)
    </#if><#elseif
    rel.type="ManyToMany">
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "${rel.foreignKey.tableName}",
            joinColumns = {@JoinColumn(name = "${rel.foreignKey.columns[0]}")},
            inverseJoinColumns = {@JoinColumn(name = "${rel.otherForeignKey.columns[0]}")})
    <#elseif rel.type="OneToMany"><#if rel.oneToOne>
    @OneToOne(<#if rel.mappedByRelationship??>mappedBy = "${rel.mappedByRelationship.attributeName}"</#if>)
    <#else>
    @OneToMany(fetch = FetchType.LAZY<#if rel.mappedByRelationship??>, mappedBy = "${rel.mappedByRelationship.attributeName}"</#if>)
    </#if>
    //cascade = {CascadeType.ALL}
    <#if !rel.oneToOne>
    //@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)</#if>
    </#if>
    public ${rel.javaType} ${rel.getter}() {
        return ${rel.attributeName};
    }

    public void ${rel.setter}(${rel.javaType} ${rel.attributeName}) {
        this.${rel.attributeName} = ${rel.attributeName};<#if
        rel.toMany && rel.oneToOne>

        if(${rel.attributeName} != null) ${rel.attributeName}.${rel.mappedByRelationship.setter}(this);</#if>
    }
    <#if rel.toMany && !rel.oneToOne>
    /**
     * method (especially for dozer) to set BOTH SIDES of the relationship
     * @param aTarget
     */
    public void add${rel.targetType.className}(${rel.targetType.className} aTarget) {
        ${rel.attributeName}.add(aTarget);
        <#if rel.mapped>if (aTarget != null) aTarget.${rel.mappedByRelationship.setter}(this);</#if>
    }

    public void remove${rel.targetType.className}(${rel.targetType.className} aTarget) {
        ${rel.attributeName}.remove(aTarget);
        <#if rel.mapped>aTarget.${rel.mappedByRelationship.setter}(null);</#if>
    }</#if>
    
</#list>

}

</#list>