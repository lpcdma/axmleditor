package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

import java.util.ArrayList;

/**
 * Created by lpcdm on 2017/9/6.
 */

public class ApplicationEditor {
    private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private final String NAME = "name";

    private String mApplicationName = "KWS_USER_ID";

    private int namespace;
    private int attr_name;
    private int application_name;

    private AXMLDoc doc;

    public ApplicationEditor(AXMLDoc doc) {
        this.doc = doc;
    }

    public void setmApplicationName(String applicationName) {
        this.mApplicationName = applicationName;
    }

    private void registStringBlock(StringBlock sb) {
        namespace = sb.putString(NAME_SPACE);
        attr_name = sb.putString(NAME);
        application_name = sb.putString(mApplicationName);
    }

    private void editNode(AXMLDoc doc) {
        BXMLNode application = doc.getApplicationNode();
        BTagNode m = (BTagNode) application;
        int name = m.getAttrStringForKey(attr_name);
        System.out.println(String.format("my main application name ==> %s", mApplicationName));
        if (name > 0) {
            System.out.println(doc.getStringBlock().getStringFor(name));
            if (name == application_name) {
                System.out.println("application name not release");
            } else {
                System.out.println("application name need release");
                if (!m.setAttrStringForKey(attr_name, application_name)){
                    System.out.println("faild!");
                }
            }
        } else {
            System.out.println("no application name");
            BTagNode.Attribute name_attr = new BTagNode.Attribute(namespace, attr_name, TypedValue.TYPE_STRING);
            name_attr.setString(application_name);
            BTagNode.Attribute[] rawAttrs = m.getAttribute();
            int namePos = 0;
            for (int i = 0; i < rawAttrs.length; i++) {
                if (rawAttrs[i].mName == doc.getStringBlock().getStringIndex("icon")) {
                    namePos = i + 1;
                }
            }
            System.out.println("namePos ==> " + namePos);
            if (namePos == 0) {
                m.setAttribute(name_attr);
            } else {
                m.setAttribute(name_attr, namePos);
            }
        }
    }

    public void commit() {
        registStringBlock(doc.getStringBlock());
        editNode(doc);
    }
}
