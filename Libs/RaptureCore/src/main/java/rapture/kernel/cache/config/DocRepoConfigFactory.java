/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rapture.kernel.cache.config;

import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.RecognitionException;

/**
 * @author bardhi
 * @since 3/9/15.
 */
public class DocRepoConfigFactory {
    /**
     * Creates a config from a standard template and an authority
     *
     * @param standardTemplate
     * @param authority
     * @return
     */
    public String createConfig(String standardTemplate, String authority) throws RecognitionException {
        standardTemplate = new FormatHelper().fillInPlaceholders(standardTemplate);
        return configWithAuthority(standardTemplate, authority);
    }

    private String configWithAuthority(String standardTemplate, String authority) {
        FormatHelper formatHelper = new FormatHelper();
        int numExpected = formatHelper.getNumExpected(standardTemplate);
        List<String> args = formatHelper.padExtras(numExpected, Arrays.asList(authority));
        return String.format(standardTemplate, args.toArray());
    }
}
