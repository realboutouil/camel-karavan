/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * PatternFly v6 Compatibility Layer
 *
 * This file provides compatibility shims for components and enums that were
 * removed or changed in PatternFly v6.
 */

import React from 'react';

/**
 * Text component - removed in PatternFly v6
 * In v6, use regular HTML elements with appropriate styling
 */
export interface TextProps extends React.HTMLAttributes<HTMLSpanElement> {
    component?: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6' | 'p' | 'a' | 'blockquote' | 'pre' | 'span';
    children?: React.ReactNode;
}

export const Text: React.FC<TextProps> = ({ component = 'span', children, ...props }) => {
    return React.createElement(component, props, children);
};

/**
 * TextContent component - removed in PatternFly v6
 */
export interface TextContentProps extends React.HTMLAttributes<HTMLDivElement> {
    children?: React.ReactNode;
}

export const TextContent: React.FC<TextContentProps> = ({ children, ...props }) => {
    return <div {...props}>{children}</div>;
};

/**
 * TextVariants enum - removed in PatternFly v6
 */
export const TextVariants = {
    h1: 'h1' as const,
    h2: 'h2' as const,
    h3: 'h3' as const,
    h4: 'h4' as const,
    h5: 'h5' as const,
    h6: 'h6' as const,
    p: 'p' as const,
    a: 'a' as const,
    small: 'small' as const,
    blockquote: 'blockquote' as const,
    pre: 'pre' as const,
};

export type TextVariants = typeof TextVariants[keyof typeof TextVariants];

/**
 * Chip component - moved to deprecated in PatternFly v6
 * Re-export for backward compatibility
 */
export { Chip } from '@patternfly/react-core/deprecated';

/**
 * NOTE: Select, SelectOption, SelectVariant, SelectDirection are NOT available
 * in PatternFly v6 deprecated components. They have been removed and replaced
 * with a new Select component with a different API.
 *
 * For now, we keep the old component usage as-is and rely on the build's
 * type checking to guide necessary updates.
 */
