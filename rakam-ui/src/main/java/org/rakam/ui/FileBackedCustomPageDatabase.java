/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rakam.ui;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.rakam.util.RakamException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by buremba <Burak Emre Kabakcı> on 11/09/15 00:50.
 */
public class FileBackedCustomPageDatabase implements CustomPageDatabase {
    private final File directory;

    @Inject
    public FileBackedCustomPageDatabase(RakamUIConfig config) {
        directory = config.getCustomPageBackendDirectory();
        directory.mkdirs();
    }

    @Override
    public void save(String project, String name, Map<String, String> files) {
        File projectDirectory = new File(directory, project);
        if (!projectDirectory.exists()) {
            projectDirectory.mkdir();
        }
        File pageDirectory = new File(projectDirectory, name);
        if(!pageDirectory.exists()) {
            pageDirectory.mkdir();
        }
        for (Map.Entry<String, String> entry : files.entrySet()) {
            try {
                // overwrite
                Files.write(new File(pageDirectory, entry.getKey()).toPath(), entry.getValue().getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public List<String> list(String project) {
        File projectDir = new File(directory, project);
        String[] list = projectDir.list();
        if(list == null) {
            return ImmutableList.of();
        }
        return Arrays.stream(list).filter(file -> new File(projectDir, file).isDirectory()).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> get(String project, String name) {
        File dir = new File(directory, project + File.separator + name);
        if(!dir.isDirectory()) {
           throw new IllegalArgumentException();
        }
        return Arrays.stream(dir.listFiles())
                .filter(File::isFile)
                .collect(Collectors.toMap(File::getName, file -> {
                    try {
                        return new String(Files.readAllBytes(file.toPath()));
                    } catch (IOException e) {
                        throw Throwables.propagate(e);
                    }
                }));
    }

    @Override
    public InputStream getFile(String project, String name, String file) {
        File f = new File(directory, project + File.separator + name + File.separator + file);
        try {
            return new ByteArrayInputStream(Files.readAllBytes(f.toPath()));
        } catch (NoSuchFileException e) {
            throw new RakamException("File not found", 404);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void delete(String project, String name) {
        File dir = new File(directory, project + File.separator + name);
        if(!dir.exists() || !dir.isDirectory()) {
           throw new IllegalArgumentException();
        }

        deleteDirectory(dir);
    }

   private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
}