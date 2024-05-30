/*
 * This file is part of CycloneDX Core (Java).
 *
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.cyclonedx.util.deserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import org.cyclonedx.model.Ancestors;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ComponentWrapper;
import org.cyclonedx.model.Descendants;
import org.cyclonedx.model.Variants;

public class ComponentWrapperDeserializer extends JsonDeserializer<ComponentWrapper>
{
  private static final String ANCESTORS = "ancestors";
  private static final String DESCENDANTS = "descendants";
  private static final String VARIANTS = "variants";

  @Override
  public ComponentWrapper deserialize(
      final JsonParser parser, final DeserializationContext context)
      throws IOException
  {
    final String location = parser.getCurrentName();
    if (parser instanceof FromXmlParser) {
      switch (location) {
        case ANCESTORS:
          return parser.readValueAs(Ancestors.class);
        case DESCENDANTS:
          return parser.readValueAs(Descendants.class);
        case VARIANTS:
          return parser.readValueAs(Variants.class);
        default:
          return null;
      }
    }

    ComponentWrapper wrapper;

    switch (location) {
      case ANCESTORS:
        wrapper = new Ancestors();
        break;
      case DESCENDANTS:
        wrapper = new Descendants();
        break;
      case VARIANTS:
        wrapper = new Variants();
        break;
      default:
        return null;
    }

    List<Component> components = Collections.emptyList();
    JsonToken currentToken = parser.currentToken();
    if (currentToken == JsonToken.START_ARRAY) {
      components = Arrays.asList(parser.readValueAs(Component[].class));
    } else if (currentToken == JsonToken.START_OBJECT) {
      // This is possible for XML input when tree has been read, then parsed with token buffer parser
      ObjectNode node = parser.readValueAs(ObjectNode.class);
      if (node.has("component")) {
        JsonNode component = node.get("component");
        try (JsonParser componentsParser = component.traverse(parser.getCodec())) {
          if (component.isArray()) {
            components = Arrays.asList(componentsParser.readValueAs(Component[].class));
          }
          else {
            components = Collections.singletonList(componentsParser.readValueAs(Component.class));
          }
        }
      }
    }
    wrapper.setComponents(components);
    return wrapper;

  }
}
