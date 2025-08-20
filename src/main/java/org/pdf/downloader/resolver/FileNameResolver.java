package org.pdf.downloader.resolver;

import org.jsoup.nodes.Element;

public interface FileNameResolver {
    String resolveFileName(Element linkElement, String context);
}