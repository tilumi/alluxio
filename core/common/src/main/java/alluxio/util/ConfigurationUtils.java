/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.util;

import static java.util.stream.Collectors.toList;

import alluxio.Configuration;
import alluxio.ConfigurationValueOptions;
import alluxio.PropertyKey;
import alluxio.util.io.PathUtils;
import alluxio.wire.ConfigProperty;
import alluxio.wire.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

/**
 * Utilities for working with Alluxio configurations.
 */
public final class ConfigurationUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

  private ConfigurationUtils() {} // prevent instantiation

  /**
   * Loads properties from resource. This method will search Classpath for the properties file with
   * the given resourceName.
   *
   * @param resourceName filename of the properties file
   * @return a set of properties on success, or null if failed
   */
  public static Properties loadPropertiesFromResource(String resourceName) {
    Properties properties = new Properties();

    InputStream inputStream =
        Configuration.class.getClassLoader().getResourceAsStream(resourceName);
    if (inputStream == null) {
      return null;
    }

    try {
      properties.load(inputStream);
    } catch (IOException e) {
      LOG.warn("Unable to load default Alluxio properties file {} : {}", resourceName,
          e.getMessage());
      return null;
    }
    return properties;
  }

  /**
   * Loads properties from the given file. This method will search Classpath for the properties
   * file.
   *
   * @param filePath the absolute path of the file to load properties
   * @return a set of properties on success, or null if failed
   */
  @Nullable
  public static Properties loadPropertiesFromFile(String filePath) {
    Properties properties = new Properties();

    try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
      properties.load(fileInputStream);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      LOG.warn("Unable to load properties file {} : {}", filePath, e.getMessage());
      return null;
    }
    return properties;
  }

  /**
   * Searches the given properties file from a list of paths as well as the classpath.
   *
   * @param propertiesFile the file to load properties
   * @param confPathList a list of paths to search the propertiesFile
   * @return the site properties file on success search, or null if failed
   */
  @Nullable
  public static String searchPropertiesFile(String propertiesFile,
      String[] confPathList) {
    if (propertiesFile == null || confPathList == null) {
      return null;
    }
    for (String path : confPathList) {
      String file = PathUtils.concatPath(path, propertiesFile);
      Properties properties = loadPropertiesFromFile(file);
      if (properties != null) {
        // If a site conf is successfully loaded, stop trying different paths.
        return file;
      }
    }
    return null;
  }

  /**
   * @return whether the configuration describes how to find the master host, either through
   *         explicit configuration or through zookeeper
   */
  public static boolean masterHostConfigured() {
    boolean usingZk = Configuration.getBoolean(PropertyKey.ZOOKEEPER_ENABLED)
        && Configuration.isSet(PropertyKey.ZOOKEEPER_ADDRESS);
    return Configuration.isSet(PropertyKey.MASTER_HOSTNAME) || usingZk;
  }

  /**
   * Gets all global configuration properties filtered by the specified scope.
   *
   * @param scope the scope to filter by
   * @return the properties
   */
  public static List<ConfigProperty> getConfiguration(Scope scope) {
    ConfigurationValueOptions useRawDisplayValue =
        ConfigurationValueOptions.defaults().useDisplayValue(true).useRawValue(true);
    return Configuration.keySet().stream()
        .filter(key -> key.getScope().contains(scope))
        .map(key -> new ConfigProperty()
            .setName(key.getName())
            .setSource(Configuration.getSource(key).toString()).setValue(
                Configuration.isSet(key) ? Configuration.get(key, useRawDisplayValue) : null))
        .collect(toList());
  }

  /**
   * @param value the value or null (value is not set)
   * @return the value or "(no value set)" when the value is not set
   */
  public static String valueAsString(String value) {
    return value == null ? "(no value set)" : value;
  }
}
