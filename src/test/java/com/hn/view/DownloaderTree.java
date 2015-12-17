package com.hn.view;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hn.controller.CentralController;
import com.hn.controller.MainController;

/**
 * 下载器的文件夹树 <br>
 * 包括：正在下载、已完成、垃圾箱
 * @author Administrator
 *
 */
public class DownloaderTree extends JTree{

	private static final long serialVersionUID = 1L;


	/**
	 * 中央控制器
	 */
	CentralController centralController;

	/**
	 * 下载器树的模型
	 */
	DefaultTreeModel treeModel;

	public DownloaderTree(){
		init();
	}

	/**
	 * 初始化
	 */
	public void init(){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("MyDownloader");

		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("正在下载");
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("已完成");
		DefaultMutableTreeNode child3 = new DefaultMutableTreeNode("垃圾箱");

		top.add(child1);
		top.add(child2);
		top.add(child3);

		treeModel = new DefaultTreeModel(top);
		this.setModel(treeModel);
		this.addListeners();
	}

	/**
	 * 添加树的监听
	 */
	public void addListeners(){
		this.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				TreePath tp = tse.getNewLeadSelectionPath();
				Object o = tp.getLastPathComponent();
				System.out.println(o);
				if("正在下载".equals(o.toString())){
					centralController.setViewStatus(MainController.RUNNING_VIEW);
				}else if("已完成".equals(o.toString())){
					centralController.setViewStatus(MainController.COMPLETE_VIEW);
				}else if("垃圾箱".equals(o.toString())){
					centralController.setViewStatus(MainController.GARBAGEBIN_VIEW);
				}
			}
		});

	}

	/**
	 * 添加中央控制器
	 * @param centralController
	 */
	public void addCentralContoller(CentralController centralController){
		if(this.centralController == null){
			this.centralController = centralController;
		}
	}
}
