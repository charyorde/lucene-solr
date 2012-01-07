package org.apache.lucene.analysis.kuromoji;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class KuromojiAnalyzer extends Analyzer {
  private final org.apache.lucene.analysis.kuromoji.Tokenizer tokenizer;
  
  public KuromojiAnalyzer(org.apache.lucene.analysis.kuromoji.Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }
  
  @Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    Tokenizer tokenizer = new KuromojiTokenizer(this.tokenizer, reader);
    return new TokenStreamComponents(tokenizer, tokenizer);
  }
}