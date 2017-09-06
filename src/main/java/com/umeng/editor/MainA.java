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
 * Created by lpcdm on 2017/9/6.
 */

public class MainA {
    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("f", true, "input AndroidManifest.xml");
            options.addOption("n", true, "application name");
            options.addOption("o", true, "out file name");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse( options, args);

            String xml = cmd.getOptionValue("f");
            String name = cmd.getOptionValue("n");
            String out = cmd.getOptionValue("o");
            if (xml == null || name == null || out == null ) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar axmleditor.jar ", options );
                return;
            }

            File axml = new File(xml);
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(axml));

            ApplicationEditor applicationEditor = new ApplicationEditor(doc);
            applicationEditor.setmApplicationName(name);
            applicationEditor.commit();
            doc.build(new FileOutputStream(new File(out)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
