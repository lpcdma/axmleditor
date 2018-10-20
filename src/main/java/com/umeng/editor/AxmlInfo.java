package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;
import com.umeng.editor.decode.BTagNode;
import com.umeng.editor.decode.BXMLNode;
import com.umeng.editor.decode.StringBlock;
import com.umeng.editor.utils.TypedValue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by lpcdm on 2017/12/28.
 */

public class AxmlInfo {
    private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private final String NAME = "name";
    private final String VALUE = "value";
    private final String LAUNCHER = "android.intent.category.LAUNCHER";
    private final String MAIN = "android.intent.action.MAIN";
    private final String DEBUG = "debuggable";
    private final String BACKUP = "allowBackup";
    private final String PACKAGE = "package";

    private int namespace;
    private int attr_name;
    private int attr_value;
    private int attr_debug;
    private int attr_backup;
    private int attr_package;

    private AXMLDoc doc;

    public AxmlInfo(AXMLDoc val) {
        this.doc = val;
    }

    public void print() {
        StringBlock sb = doc.getStringBlock();
        namespace = sb.putString(NAME_SPACE);
        attr_name = sb.putString(NAME);
        attr_value = sb.putString(VALUE);
        attr_debug = sb.putString(DEBUG);
        attr_backup = sb.putString(BACKUP);
        attr_package = sb.putString(PACKAGE);

        BXMLNode root = doc.getManifestNode();
        BTagNode r = (BTagNode) root;
        int app_package = r.getAttrStringForKey(attr_package);
        String package_name = null;
        if (app_package > 0) {
            package_name = doc.getStringBlock().getStringFor(app_package);
        }

        BXMLNode application = doc.getApplicationNode();
        BTagNode m = (BTagNode) application;

        int[] app_debug = m.getAttrValueForKey(attr_debug);
        if (app_debug != null) {
            if (app_debug[0] == TypedValue.TYPE_INT_BOOLEAN && app_debug[1] != 0xffffffff) {
                m.setAttrValueForKey(attr_debug, TypedValue.TYPE_INT_BOOLEAN, 0xffffffff);
            }
        } else {
            BTagNode.Attribute debug_attr = new BTagNode.Attribute(namespace, attr_debug, TypedValue.TYPE_INT_BOOLEAN);
            debug_attr.setValue(TypedValue.TYPE_INT_BOOLEAN, 0xffffffff);
            BTagNode.Attribute[] rawAttrs = m.getAttribute();
            int namePos = 0;
            for (int i = 0; i < rawAttrs.length; i++) {
                if (rawAttrs[i].mName == doc.getStringBlock().getStringIndex(BACKUP)) {
                    namePos = i;
                }
            }
            if (namePos == 0) {
                m.setAttribute(debug_attr);
            } else {
                m.setAttribute(debug_attr, namePos);
            }
        }

        int app_name = m.getAttrStringForKey(attr_name);
        if (app_name > 0) {
            if (doc.getStringBlock().getStringFor(app_name).substring(0, 1).equals(".")) {
                System.out.println(package_name + doc.getStringBlock().getStringFor(app_name));
            } else {
                System.out.println(doc.getStringBlock().getStringFor(app_name));
            }
        } else {
            List<BXMLNode> childList = m.getChildren();
            for (BXMLNode child : childList) {
                BTagNode c = (BTagNode) child;
                if ("provider".equals(doc.getStringBlock().getStringFor(c.getName()))) {
                    if (doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)).substring(0, 1).equals(".")) {
                        System.out.println(package_name + doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                    } else {
                        System.out.println(doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                    }
                }
                if ("activity".equals(doc.getStringBlock().getStringFor(c.getName())) && c.getChildren() != null) {
                    boolean isL = false;
                    boolean isM = false;
                    for (BXMLNode cif : c.getChildren()) {
                        BTagNode if_ = (BTagNode) cif;
                        if ("intent-filter".equals(doc.getStringBlock().getStringFor(if_.getName())) && if_ != null) {
                            for (BXMLNode ac : if_.getChildren()) {
                                BTagNode ac_ = (BTagNode) ac;
                                if (LAUNCHER.equals(doc.getStringBlock().getStringFor(ac_.getAttrStringForKey(attr_name)))) {
                                    isL = true;
                                }
                                if (MAIN.equals(doc.getStringBlock().getStringFor(ac_.getAttrStringForKey(attr_name)))) {
                                    isM = true;
                                }
                            }
                        }
                    }

                    if (isL) {
                        if (doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)).substring(0, 1).equals(".")) {
                            System.out.println(package_name + doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                        } else {
                            System.out.println(doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                        }
                    }
                    if (!isL && isM) {
                        if (doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)).substring(0, 1).equals(".")) {
                            System.out.println(package_name + doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                        } else {
                            System.out.println(doc.getStringBlock().getStringFor(c.getAttrStringForKey(attr_name)));
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("f", true, "input AndroidManifest.xml");
            options.addOption("o", true, "out file name");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String xml = cmd.getOptionValue("f");
            if (xml == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar axmleditor.jar ", options);
                return;
            }

            File axml = new File(xml);
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(axml));

            AxmlInfo axmlInfo = new AxmlInfo(doc);
            axmlInfo.print();

            String out = cmd.getOptionValue("o");
            if (out != null) {
                doc.build(new FileOutputStream(new File(out)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
