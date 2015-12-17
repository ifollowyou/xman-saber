package com.hn.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * 下载器的下部
 * @author Administrator
 *
 */
public class DownloaderFoot extends JLabel {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造器
	 */
	public DownloaderFoot(){
		super("Thank you for your use!");
		init();
	}

	/**
	 * 初始化
	 */
	private void init(){

		this.setBorder(BorderFactory.createEtchedBorder(1));// 创建一个具有“浮雕化”外观效果的边框，将组件的当前背景色用于突出显示和阴影显示

	}
}
