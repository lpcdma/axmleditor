package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

import java.util.List;

/**
 * Created by lpcdm on 2017/9/7.
 */

public class PermissionEditor {
    private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private final String NAME = "name";
    private final String USES_PERMISSION = "uses-permission";
    private final String USES_SDK = "uses-sdk";

    private String mPermissionName = "KWS_USER_ID";

    private int namespace;
    private int attr_name;
    private int permission_name;
    private int uses_permission;
    private int uses_sdk;

    private AXMLDoc doc;

    public PermissionEditor(AXMLDoc doc) {
        this.doc = doc;
    }

    public void setmPermissionName(String permissionName) {
        this.mPermissionName = permissionName;
    }

    private void registStringBlock(StringBlock sb) {
        namespace = sb.putString(NAME_SPACE);
        attr_name = sb.putString(NAME);
        uses_permission = sb.putString(USES_PERMISSION);
        permission_name = sb.putString(mPermissionName);
        uses_sdk = sb.putString(USES_SDK);
    }

    private void editNode(AXMLDoc doc) {
        BXMLNode manifest = doc.getManifestNode();
        List<BXMLNode> children = manifest.getChildren();

        BTagNode tmp_permission = null;
        int tmpSdkIndex = 0;
        int sdkIndex = 0;

        end:
        for (BXMLNode node : children) {
            BTagNode m = (BTagNode) node;
            tmpSdkIndex += 1;
            if (uses_sdk == m.getName()) {
                sdkIndex = tmpSdkIndex;
            }
            if ((uses_permission == m.getName()) && (m.getAttrStringForKey(attr_name) == permission_name)) {
                tmp_permission = m;
                break end;
            }
        }
        System.out.println("sdkIndex ==> " + sdkIndex);
        if (tmp_permission == null) {
            System.out.println("not find this permission now add it!");
            BTagNode.Attribute name_attr = new BTagNode.Attribute(namespace, attr_name, TypedValue.TYPE_STRING);
            name_attr.setString(permission_name);
            tmp_permission = new BTagNode(-1, uses_permission);
            tmp_permission.setAttribute(name_attr);
            if (sdkIndex > 0) {
                children.add(sdkIndex, tmp_permission);
            } else {
                children.add(tmp_permission);
            }
        }
    }

    public void commit() {
        registStringBlock(doc.getStringBlock());
        editNode(doc);
    }
}
