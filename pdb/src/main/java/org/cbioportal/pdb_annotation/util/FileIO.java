package org.cbioportal.pdb_annotation.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

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
        Resource resource = new ClassPathResource(resourceURI);

        try {
            return new InputStreamReader(resource.getInputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to read input", e);
        } catch (IOException e) {
            throw new IllegalStateException("Input not found", e);
        }
    }
}
