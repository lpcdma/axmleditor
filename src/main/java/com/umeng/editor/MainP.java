package com.umeng.editor;

import com.umeng.editor.decode.AXMLDoc;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by lpcdm on 2017/9/7.
 */

public class MainP {
    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("f", true, "input AndroidManifest.xml");
            options.addOption("n", true, "permission name p1[p1,p2,p3]");
            options.addOption("o", true, "out file name");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String xml = cmd.getOptionValue("f");
            String name = cmd.getOptionValue("n");
            String out = cmd.getOptionValue("o");
            if (xml == null || name == null || out == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar axmleditor.jar ", options);
                return;
            }

            File axml = new File(xml);
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(axml));

            PermissionEditor permissionEditor = new PermissionEditor(doc);
            if (name.contains(",")) {
                String names[] = name.split(",");
                for (int i = 0; i < names.length; i++) {
                    permissionEditor.setmPermissionName(names[i]);
                    permissionEditor.commit();
                }
            } else {
                permissionEditor.setmPermissionName(name);
                permissionEditor.commit();
            }
            doc.build(new FileOutputStream(new File(out)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
