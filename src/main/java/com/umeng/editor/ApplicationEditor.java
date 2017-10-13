package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.ResBlock;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lpcdm on 2017/9/6.
 */

public class ApplicationEditor {
    private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private final String NAME = "name";
    private final String META_DATA = "meta-data";
    private final String VALUE = "value";
    private String mApplicationName = "KWS_USER_ID";
    public final int STATIC_VAALE = 0x01010024;

    private int namespace;
    private int attr_name;
    private int application_name;

    private String mChannelName = "KWS_MAIN_APP";
    private String mChannelValue = "android.app.Application";

    private int meta_data;
    private int attr_value;
    private int channel_name;
    private int channel_value = -1;

    private AXMLDoc doc;

    public ApplicationEditor(AXMLDoc doc) {
        this.doc = doc;
    }

    public void setmApplicationName(String applicationName) {
        this.mApplicationName = applicationName;
    }

    private void registStringBlockMetaData(StringBlock sb) {
        meta_data = sb.putString(META_DATA);
        attr_value = sb.putString(VALUE);
        channel_name = sb.putString(mChannelName);

        if(channel_value == -1){
            channel_value = sb.addString(mChannelValue);//now we have a seat in StringBlock
        }
    }

    public void setChannel(String channel) {
        mChannelValue = channel;
    }

    private void registStringBlock(StringBlock sb) {
        namespace = sb.putString(NAME_SPACE);
        attr_name = sb.putString(NAME);
        application_name = sb.putString(mApplicationName);
    }

    private void editMetaData(AXMLDoc doc) {
        BXMLNode application = doc.getApplicationNode();
        List<BXMLNode> children = application.getChildren();

        BTagNode umeng_meta = null;

        end:
        for (BXMLNode node : children) {
            BTagNode m = (BTagNode) node;
            //it's a risk that the value for "android:name" maybe not String
            if ((meta_data == m.getName()) && (m.getAttrStringForKey(attr_name) == channel_name)) {
                umeng_meta = m;
                break end;
            }
        }

        if (umeng_meta != null) {
            umeng_meta.setAttrStringForKey(attr_value, channel_value);
        } else {
            BTagNode.Attribute name_attr = new BTagNode.Attribute(namespace, attr_name, TypedValue.TYPE_STRING);
            name_attr.setString(channel_name);
            BTagNode.Attribute value_attr = new BTagNode.Attribute(namespace, attr_value, TypedValue.TYPE_STRING);
            value_attr.setString( channel_value );

            umeng_meta = new BTagNode(-1, meta_data);
            umeng_meta.setAttribute(name_attr);
            umeng_meta.setAttribute(value_attr);

            children.add(umeng_meta);
        }
    }

    private void editNode(AXMLDoc doc) {
        BXMLNode application = doc.getApplicationNode();
        BTagNode m = (BTagNode) application;
        int name = m.getAttrStringForKey(attr_name);
        System.out.println(String.format("my main application name ==> %s", mApplicationName));
        if (name > 0) {
            System.out.println(doc.getStringBlock().getStringFor(name));
            setChannel(doc.getStringBlock().getStringFor(name));
            registStringBlockMetaData(doc.getStringBlock());
            editMetaData(doc);
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
            registStringBlockMetaData(doc.getStringBlock());
            editMetaData(doc);
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

    private void checkResValue() {
        ResBlock resBlock = doc.getResBlock();
        boolean hasValue = false;
        int idSize = resBlock.getResourceIds().length;
        for (int i = 0; i < idSize; i++) {
            int id = resBlock.getResourceIdAt(i);
            //System.out.println(String.format("id ==> %x", id));
            if (id == STATIC_VAALE) {
                hasValue = true;
            }
        }
        if (!hasValue) {
            int newIdSize = doc.getStringBlock().getStringIndex(VALUE);
            //System.out.println(String.format("newIdSize ==> %x", newIdSize));
            int[] newRawResIds = new int[newIdSize + 1];
            for (int i = 0; i < newRawResIds.length; i++) {
                newRawResIds[i] = 0;
            }
            int[] rawResIds = resBlock.getResourceIds();
            System.arraycopy(rawResIds, 0, newRawResIds, 0, idSize);
            newRawResIds[newIdSize] = STATIC_VAALE;
            resBlock.setRawResIds(newRawResIds);
        }
    }

    public void commit() {
        registStringBlock(doc.getStringBlock());
        checkResValue();
        editNode(doc);
    }
}
