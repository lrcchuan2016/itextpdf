/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2013 1T3XT BVBA
 * Authors: VVB, Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.tool.xml.svg;

import java.util.Map;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.PipelineException;
import com.itextpdf.tool.xml.ProcessObject;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.WorkerContext;
import com.itextpdf.tool.xml.Writable;
import com.itextpdf.tool.xml.pipeline.AbstractPipeline;
import com.itextpdf.tool.xml.pipeline.ctx.MapContext;
import com.itextpdf.tool.xml.svg.graphic.Svg;
import com.itextpdf.tool.xml.svg.tags.Graphic;
import com.itextpdf.tool.xml.svg.utils.TransformationMatrix;


/**
 * This pipeline writes to a Document.
 * @author redlab_b
 *
 */
public class PdfTemplatePipeline extends AbstractPipeline<MapContext> {

	private PdfTemplate template;
	
	/**
	 * @param doc the document
	 * @param writer the writer
	 */
	public PdfTemplatePipeline(PdfContentByte cb) {
		super(null);
		template = cb.createTemplate(0, 0);
	}

	/**
	 * @param po
	 * @throws PipelineException
	 */
	
	//lijst met graphische elementen (en andere?)
	private void write(final WorkerContext context, final ProcessObject po, final Tag t) throws PipelineException {
		
		
		//MapContext mp = getLocalContext(context);
		if (po.containsWritable()) {
			//Document doc = (Document) mp.get(DOCUMENT);
			//boolean continuousWrite = (Boolean) mp.get(CONTINUOUS);
			Writable writable = null;
			while (null != (writable = po.poll())) {
				if (writable instanceof Graphic) {
					((Graphic) writable).draw(template, t.getCSS());
					if (writable instanceof Svg) {
						Svg svg = (Svg)writable;
						Rectangle viewBox = svg.getViewBox();
						template.setBoundingBox(new Rectangle(viewBox.getWidth(), viewBox.getHeight()));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.itextpdf.tool.xml.pipeline.Pipeline#open(com.itextpdf.tool.
	 * xml.Tag, com.itextpdf.tool.xml.pipeline.ProcessObject)
	 */
	@Override
	public Pipeline<?> open(final WorkerContext context, final Tag t, final ProcessObject po) throws PipelineException {		

		Map<String, String> attributes = t.getAttributes();
		if(attributes != null){
			String transform = attributes.get("transform");
			if(transform != null){
				AffineTransform matrix = TransformationMatrix.getTransformationMatrix(transform);
				if(matrix != null){
					template.concatCTM(matrix);
				}
			}
		}		
		write(context, po, t);

		return getNext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.itextpdf.tool.xml.pipeline.Pipeline#content(com.itextpdf.tool
	 * .xml.Tag, java.lang.String, com.itextpdf.tool.xml.pipeline.ProcessObject)
	 */
	@Override
	public Pipeline<?> content(final WorkerContext context, final Tag currentTag, final String text, final ProcessObject po) throws PipelineException {
		write(context, po, currentTag);
		return getNext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.itextpdf.tool.xml.pipeline.Pipeline#close(com.itextpdf.tool
	 * .xml.Tag, com.itextpdf.tool.xml.pipeline.ProcessObject)
	 */
	@Override
	public Pipeline<?> close(final WorkerContext context, final Tag t, final ProcessObject po) throws PipelineException {		
		write(context ,po, t);
		//writer.getDirectContent().restoreState();
		return getNext();
	}
	
	public PdfTemplate getTemplate() {
		return template;
	}
}
