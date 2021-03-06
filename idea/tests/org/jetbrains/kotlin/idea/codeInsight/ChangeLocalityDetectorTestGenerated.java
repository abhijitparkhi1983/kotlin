/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.codeInsight;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/codeInsight/changeLocality")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ChangeLocalityDetectorTestGenerated extends AbstractChangeLocalityDetectorTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInChangeLocality() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/codeInsight/changeLocality"), Pattern.compile("^(.+)\\.(kt|kts)$"), null, true);
    }

    @TestMetadata("Comment.kt")
    public void testComment() throws Exception {
        runTest("idea/testData/codeInsight/changeLocality/Comment.kt");
    }

    @TestMetadata("InMethod.kt")
    public void testInMethod() throws Exception {
        runTest("idea/testData/codeInsight/changeLocality/InMethod.kt");
    }
}
