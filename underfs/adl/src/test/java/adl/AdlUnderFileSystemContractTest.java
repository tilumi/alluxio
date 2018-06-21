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

package adl;

import alluxio.underfs.AbstractUnderFileSystemContractTest;
import alluxio.underfs.UnderFileSystem;
import alluxio.underfs.UnderFileSystemConfiguration;
import com.google.common.base.Preconditions;
import org.junit.BeforeClass;

/**
 * This UFS contract test will use Wasb as the backing store.
 */
public final class AdlUnderFileSystemContractTest extends AbstractUnderFileSystemContractTest {
  private static final String ADL_BASE_DIR_CONF = "testAdlBaseDir";
  private static final String ADL_BASE_DIR = System.getProperty(ADL_BASE_DIR_CONF);

  @BeforeClass
  public static void beforeClass() throws Exception {
    Preconditions.checkNotNull(ADL_BASE_DIR, "ADL_BASE_DIR");
    Preconditions.checkState(new AdlUnderFileSystemFactory().supportsPath(ADL_BASE_DIR),
        "%s is not a valid WASB path", ADL_BASE_DIR);
  }

  @Override
  public UnderFileSystem createUfs(String path, UnderFileSystemConfiguration conf)
      throws Exception {
    return new AdlUnderFileSystemFactory().create(path, conf);
  }

  @Override
  public String getUfsBaseDir() {
    return ADL_BASE_DIR;
  }
}
