package com.giisoo.app.web;

import java.io.OutputStream;

import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

public class svg extends Model {

	@Path(path = "download", log = Model.METHOD_POST)
	public void download() {
		try {
			this.req.setCharacterEncoding("UTF-8");
			String fileName = this.getString("filename");
			String svg = this.getHtml("svg", true); // get all
			String type = this.getString("type");

			if (ChartsType.image_svg_xml.getChartsTypeName().equalsIgnoreCase(
					type)) {
				// 复位response
				setContentType(ChartsType.contentType.getChartsTypeName());
				setHeader(ChartsType.content_Disposition.getChartsTypeName(),
						ChartsType.attachment.getChartsTypeName() + fileName);
				OutputStream sos = this.getOutputStream();
				sos.write(svg.getBytes());
				sos.flush();
				sos.close();
			} else {
				this.print("not support");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public enum FileExtendsName {

		svg(".svg"), pdf(".pdf"), png(".png"), jpg(".jpg");

		private String getFileExtendName;

		public String getGetFileExtendName() {
			return getFileExtendName;
		}

		private FileExtendsName(String getFileExtendName) {
			this.getFileExtendName = getFileExtendName;
		}
	}

	public enum ChartsType {

		image_png("image/png"), image_jpeg("image/jpeg"), image_svg_xml(
				"image/svg+xml"), contentType("application/x-download"), content_Disposition(
				"Content-Disposition"), attachment("attachment;filename=");

		private String chartsTypeName;

		private ChartsType(String chartsTypeName) {
			this.chartsTypeName = chartsTypeName;
		}

		public String getChartsTypeName() {
			return chartsTypeName;
		}

		public void setChartsTypeName(String chartsTypeName) {
			this.chartsTypeName = chartsTypeName;
		}

	}
}
