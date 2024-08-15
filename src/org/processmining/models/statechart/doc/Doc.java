package org.processmining.models.statechart.doc;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Doc {

    public static boolean openDoc() {
        if (Desktop.isDesktopSupported()) {
            try {
                String inputPdf = "Manual.pdf";
                Path tempOutput = Files.createTempFile("Statechart-ProM-Manual-", ".pdf");
                tempOutput.toFile().deleteOnExit();
                try (InputStream is = Doc.class.getResourceAsStream(inputPdf)) {
                    Files.copy(is, tempOutput, StandardCopyOption.REPLACE_EXISTING);
                }
                Desktop.getDesktop().open(tempOutput.toFile());
                return true;
            } catch(IOException e) {
                // nop, return false
            }
        }
        
        return false;
    }
}
