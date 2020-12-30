package ptithcm.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import ptithcm.entity.Category;
import ptithcm.entity.Image;
import ptithcm.entity.News;
import ptithcm.entity.Paragraph;

@Controller
@RequestMapping("")
@Transactional
public class AdminController {

	@Autowired
	SessionFactory factory;
	@Autowired
	ServletContext context;

	@RequestMapping("admin/index")
	public String admin_index() {
		return "admin/index";
	}
	
	@RequestMapping("admin/insert/paragraph")
	@ResponseBody
	public String addParagraph(HttpServletRequest request) {
		try {
			request.getSession().setAttribute("count", Integer.parseInt(request.getSession().getAttribute("count").toString()) + 1);
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(request.getSession().getAttribute("count").toString());
		return "";
	}

	public boolean validateN(ModelMap model, News news, BindingResult errors) {
		if (news.getTitle().trim().equals("")) {
			errors.rejectValue("title", "news", "Title is empty!");
		}
		if (news.getDescription().trim().equals("")) {
			errors.rejectValue("description", "news", "Description is empty!");
		}

		/*
		 * if (photo == null) { errors.rejectValue("image", "news", "Photo is empty"); }
		 * else { String extention =
		 * photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf
		 * (".") + 1); if (!extention.equalsIgnoreCase("jpg")) {
		 * errors.rejectValue("image", "news", "Photo is not suitble"); } }
		 */

		if (errors.hasErrors()) {
			model.addAttribute("message", "Do it!");
			return false;
		}
		return true;
	}

	public boolean validateC(ModelMap model, Category cate, BindingResult errors) {
		if (cate.getName().trim() == "") {
			errors.rejectValue("name", "cate", "Name is empty");
		}
		if (errors.hasErrors()) {
			model.addAttribute("message", "Do it!");
			return false;
		}
		return true;
	}

	@RequestMapping(value = "admin/insert-news")
	public String insert_news(ModelMap model, HttpServletRequest request) {
		Session session = null;
		try {
			request.getSession().getAttribute("count");
		} catch (Exception e) {
			request.getSession().setAttribute("count", 1);
		}
		try {
			session = factory.getCurrentSession();
			String hql = "SELECT MAX(id) FROM News";
			Query query = session.createQuery(hql);
			int id = (int) query.uniqueResult();
			News news = new News();
			news.setId(id + 1);
			model.addAttribute("news", news);
			return "admin/insert-news";
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "redirect:/admin/index.htm";
	}

	@RequestMapping(value = "admin/insert-news", params = "btnSave")
	public String insert_news(ModelMap model, @ModelAttribute("news") News news,
			@RequestParam("image_header") MultipartFile image_header, HttpServletRequest request,
			@RequestParam("paraTitle") String[] paraTitle, @RequestParam("paraQuote") String[] paraQuote,
			@RequestParam("paraContent") String[] paraContent, @RequestParam("paraImage") MultipartFile[] paraImage,
			BindingResult errors) {

		System.out.println(request.getParameter("countiu"));
		Session session = null;
		Transaction t = null;
		if (validateN(model, news, errors)) {
			try {
				session = factory.openSession();
				t = session.beginTransaction();
				String hql = "SELECT MAX(id) FROM Image";
				Query query = session.createQuery(hql);
				int ImageId = (int) query.uniqueResult();

				hql = "SELECT MAX(id) FROM Paragraph";
				query = session.createQuery(hql);
				int ParaId = (int) query.uniqueResult();
				System.out.println("paraContent: "+paraContent[0]);
	

				String fname = image_header.getOriginalFilename();
				int i = 1;
				String newFileName;
				String path;
				try {
					newFileName = news.getId() + "-" + i + fname.substring(fname.lastIndexOf("."));
					path = context
							.getRealPath("./images/web/" + news.getId() + fname.substring(fname.lastIndexOf(".")));
					image_header.transferTo(new File(path));
					news.setLink(newFileName);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				session.save(news);
				// paragraph and image
				/*for (int j = 0; j < paraTitle.length; j++) {
					Paragraph para = new Paragraph();
					para.setId(ParaId + 1);
					para.setNews(news);
					para.setPara_content(paraContent[j]);
					para.setQuote(paraQuote[j]);
					para.setTitle(paraTitle[j]);
					ParaId++;
					j++;

					session.save(para);
					System.out.println("Save Para "+ j);
					++i;
					try {
						fname = paraImage[j].getOriginalFilename();
						System.out.println("fname= " + fname);
						newFileName = news.getId() + "-" + i + fname.substring(fname.lastIndexOf("."));
						path = context.getRealPath("./images/web/" + newFileName);
						paraImage[j].transferTo(new File(path));
						
						Image im = new Image();
						im.setId(++ImageId);
						im.setLink(newFileName);
						im.setPara(para);
						session.save(im);
					} catch (Exception e) {
						// TODO: handle exception
					}

					
				}*/

				t.commit();
				model.addAttribute("message", "Success!");
				return "redirect:/admin/index.htm";
			} catch (Exception e) {
				t.rollback();
				model.addAttribute("message", "Failure!");
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
		return "admin/insert-news";

	}

	@RequestMapping(value = "admin/insert-news", params = "btnReset")
	public String resetIN(ModelMap model, @ModelAttribute("news") News news) {
		news.setTitle(null);
		news.setDescription(null);
		news.setCate(null);
		return "admin/insert-news";
	}

	@RequestMapping(value = "admin/insert-news", params = "btnExit")
	public String exitIN() {
		return "redirect:/admin/index.htm";
	}

	@RequestMapping(value = "admin/editN/{id}")
	public String edit(ModelMap model, @PathVariable("id") int id) {
		Session session = null;
		try {
			session = factory.getCurrentSession();
			String hql = "FROM News WHERE id= " + id;
			Query query = session.createQuery(hql);
			List<News> list = query.list();
			model.addAttribute("news", list.get(0));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "admin/editN";
	}

	@RequestMapping(value = "admin/editN", params = "btnSave")
	public String edit(ModelMap model, @ModelAttribute("news") News news, BindingResult errors) {
		Session session = null;
		Transaction t = null;
		if (validateN(model, news, errors)) {
			try {
				session = factory.openSession();
				t = session.beginTransaction();
				session.update(news);
				t.commit();
				model.addAttribute("message", "Success!");
			} catch (Exception e) {
				model.addAttribute("message", "Failure!");
				t.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
		return "redirect:/admin/index.htm";
	}

	@RequestMapping(value = "admin/editN", params = "btnReset")
	public String edit(@ModelAttribute("n") News news) {
		return "redirect:/admin/editN/" + news.getId() + ".htm";
	}

	@RequestMapping(value = "admin/editN", params = "btnExit")
	public String edit(ModelMap model) {
		return "admin/index";
	}

	public Boolean deleteI(int id) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		try {
			Query query = session.createQuery("FROM Image WHERE news_id = " + id);
			List<Image> list = query.list();
			for (Image image : list) {
				session.delete(image);
			}
			t.commit();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			t.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return false;
	}

	@RequestMapping(value = "admin/deleteN/{id}")
	public String deleteN(ModelMap model, @PathVariable("id") int id) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		try {
			News n = new News();
			n.setId(id);
			session.delete(n);
			t.commit();
			model.addAttribute("message", "Success!");
		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("message", "Failure!");
			t.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "redirect:/admin/index.htm";
	}

	public void deleteN(int id) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		try {
			Query query = session.createQuery("FROM News WHERE cate_id = " + id);
			List<News> list = query.list();
			for (News news : list) {
				session.delete(news);
			}
			t.commit();
		} catch (Exception e) {
			// TODO: handle exception
			t.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	@RequestMapping(value = "admin/insert-cate")
	public String insert(ModelMap model) {
		Category cate = new Category();
		model.addAttribute("cate", cate);
		return "admin/insert-cate";
	}

	@RequestMapping(value = "admin/insert-cate", params = "btnSave")
	public String insert(ModelMap model, @ModelAttribute("cate") Category cate, BindingResult errors) {
		Session session = null;
		Transaction t = null;
		if (validateC(model, cate, errors)) {
			try {
				session = factory.openSession();
				t = session.beginTransaction();
				session.save(cate);
				t.commit();
				model.addAttribute("message", "Success!");
				return "redirect:/admin/index.htm";
			} catch (Exception e) {
				model.addAttribute("message", "Failure!");
				t.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
		return "admin/insert-cate";

	}

	@RequestMapping(value = "admin/insert-cate", params = "btnReset")
	public String reset(ModelMap model, @ModelAttribute("cate") Category cate) {
		cate.setName(null);
		cate.setDesign_type(0);
		return "admin/insert-cate";
	}

	@RequestMapping(value = "admin/insert-cate", params = "btnExit")
	public String exit(ModelMap model) {
		return "admin/index";
	}

	@RequestMapping(value = "admin/editC/{id}")
	public String exitC(ModelMap model, @PathVariable("id") int id) {
		Session session = null;
		try {
			session = factory.openSession();
			Category cate = (Category) session.get(Category.class, id);
			model.addAttribute("cate", cate);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			session.close();
		}
		return "admin/editC";
	}

	@RequestMapping(value = "admin/editC", params = "btnSave")
	public String edit(ModelMap model, @ModelAttribute("cate") Category cate, BindingResult errors) {
		Session session = null;
		Transaction t = null;
		if (validateC(model, cate, errors)) {
			try {
				session = factory.openSession();
				t = session.beginTransaction();
				session.update(cate);
				t.commit();
				model.addAttribute("message", "Success!");
			} catch (Exception e) {
				model.addAttribute("message", "Failure!");
				e.printStackTrace();
				t.rollback();
			} finally {
				session.close();
			}
		}
		return "redirect:/admin/index.htm";
	}

	@RequestMapping(value = "admin/editC", params = "btnReset")
	public String resetC(ModelMap model, @ModelAttribute("cate") Category cate) {
		return "redirect:/admin/editC/" + cate.getId() + ".htm";
	}

	@RequestMapping(value = "admin/editC", params = "btnExit")
	public String exitC() {
		return "admin/index";
	}

	@RequestMapping(value = "admin/deleteC/{id}")
	@ResponseBody // nhận giá trị trả về
	public String deleteC(ModelMap model, @PathVariable("id") int id) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		try {

			deleteN(id);
			Category c = new Category();
			c.setId(id);
			session.delete(c);
			t.commit();
			model.addAttribute("message", "Success!");
		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("message", "Failure!");
			t.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "redirect:/admin/index.htm";
	}

	@ModelAttribute("admin_news")
	public List<News> getAdNews() {
		Session session = factory.getCurrentSession();
		String hql = "FROM News";
		Query query = session.createQuery(hql);
		List<News> list = query.list();
		return list;
	}

	@ModelAttribute("cates")
	public List<News> getCase() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Category";
		Query query = session.createQuery(hql);
		List<News> list = query.list();
		return list;
	}
}
