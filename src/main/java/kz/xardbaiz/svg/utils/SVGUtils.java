package kz.xardbaiz.svg.utils;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGLine;
import org.apache.batik.svggen.SVGPath;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import kz.xardbaiz.svg.utils.models.VectorFigure;

// TODO move to separate repo

public class SVGUtils {

	public static SVGDocument createSvgDocument(float width, float height) {
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
		SVGDocument document = (SVGDocument) domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg",
				null);
		Element svgTag = document.getRootElement();
		svgTag.setAttribute("width", String.valueOf(width));
		svgTag.setAttribute("height", String.valueOf(height));
		return document;
	}

	public static void putPathToSvgDocument(SVGDocument document, GeneralPath path) {
		putPathToSvgDocument(document, new VectorFigure(path, null, Color.BLACK));
	}

	public static void putPathToSvgDocument(SVGDocument document, VectorFigure figure) {
		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		
		SVGPath svgPath = new SVGPath(ctx);
		Element svgElement = svgPath.toSVG(figure.getFigurePath());

		Color nonStrokingColor = figure.getNonStrokingColor();
		if (nonStrokingColor != null) {
			svgElement.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, getRgbColorString(nonStrokingColor));
			
			double alphaConstant = figure.getNonStrokeAlphaConstant();
			if (alphaConstant!=1) {
				svgElement.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, String.valueOf(alphaConstant));
			}
		}
		
		processStroke(figure, svgElement);
		setVisibility(figure, svgElement);
		document.getRootElement().appendChild(svgElement);
	}
	
	private static void processStroke(VectorFigure figure, Element svgElement) {

		Color strokingColor = figure.getStrokingColor();

		// Stroke check
		if (strokingColor != null) {
			svgElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, getRgbColorString(strokingColor));
			svgElement.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, String.valueOf(figure.getStrokeWidth()));
			svgElement.setAttribute(SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE, figure.getLineCap());
			svgElement.setAttribute(SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE, figure.getLineJoin());

			String dashArray = figure.getDashArray();
			if (dashArray != null && !"".equals(dashArray)) {
				svgElement.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE, dashArray);
			}

			double alphaConstant = figure.getAlphaConstant();
			if (alphaConstant != 1) {
				svgElement.setAttribute(SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE, String.valueOf(alphaConstant));
			}

			// TODO 'stroke-dashoffset'
		}
	}
	
	private static void setVisibility(VectorFigure figure, Element svgElement) {
		// visibility="visible" is default value
		if (figure.getStrokingColor() == null && figure.getNonStrokingColor() == null) {
			svgElement.setAttribute(SVGConstants.CSS_VISIBILITY_PROPERTY, SVGConstants.CSS_HIDDEN_VALUE);
		} else if (figure.getStrokingColor() != null && figure.getNonStrokingColor() == null) {
			svgElement.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, "transparent");
		}
	}

	private static String getRgbColorString(Color color) {
		return String.format("rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
	}

	public static void saveSvgDocumentToFile(SVGDocument document, File file)
			throws FileNotFoundException, IOException {
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		try (Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			svgGenerator.stream(document.getDocumentElement(), out);
		}
	}
}
