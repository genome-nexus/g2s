package org.cbioportal.pdb_annotation.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.*;

/**
 * @author Selcuk Onur Sumer
 */
public class FileIO
{
    /**
     * Creates a reader for a resource in the relative path
     *
     * @param resourceURI path of the resource to be read
     * @return a reader of the resource
     */
    public static Reader getReader(String resourceURI)
    {
        // first try class path
        Resource resource = new ClassPathResource(resourceURI);

        // if not exists then try absolute path
        if (!resource.exists()) {
            resource = new PathResource(resourceURI);
        }

        try {
            return new InputStreamReader(resource.getInputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to read input", e);
        } catch (IOException e) {
            throw new IllegalStateException("Input not found", e);
        }
    }
}
