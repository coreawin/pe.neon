/**
 *
 */
package pe.neon.hkchoi;

import com.diquest.scopus.schema.bean.ani515.*;

import javax.xml.bind.JAXBElement;

import re.kisti.mirian.load.scopus.ani515.tablebean.TableBean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author neon
 * @date 2022.03.16
 * @Version 1.0
 */
public class Author_GroupTable extends TableBean {
    /**
     * eid
     */
    private String eid;

    /**
     * authorgroup_list
     */
    private LinkedList<Author_Group_TP> authorgroup_list;

    /**
     * author_list
     */
    private LinkedList<Author> author_list;

    private LinkedList<Collaboration> collaborationList;

    /**
     * checkDuplication
     */
    private HashSet<String> checkDuplication = null;

    private boolean existOrcId = false;

    /**
     * correspondAuthor
     */
    // private LinkedList<CorrespondAuthorTable> correspondAuthor;

    /*
     * (non-Javadoc)
     *
     * @see re.kisti.mirian.load.scopus.tablebean.TableBean#initValues(re.kisti.
     * mirian .load.scopus.bean.DocTp)
     */
    public void initValues(DocTp document) {
        author_list = new LinkedList<Author>();
        setAuthorGroup(document);
    }

    @SuppressWarnings("unchecked")
    private void setAuthorGroup(DocTp document) {
        List<AuthorGroupTp> groupTp = document.getItem().getItem().getBibrecord().getHead().getAuthorGroup();
        checkDuplication = new HashSet<String>();
        authorgroup_list = new LinkedList<Author_Group_TP>();
        collaborationList = new LinkedList<Collaboration>();
        // correspondAuthor = new LinkedList<CorrespondAuthorTable>();
        author_list = new LinkedList<Author>();

        this.eid = document.getMeta().getEid();
        for (int group_seq = 0; group_seq < groupTp.size(); group_seq++) {
            AuthorGroupTp agTp = groupTp.get(group_seq);
            AffiliationTp a = agTp.getAffiliation();
            List<Object> aoc = agTp.getAuthorOrCollaboration();
            for (int idx = 0; idx < aoc.size(); idx++) {
                Object ao = aoc.get(idx);
                if (ao instanceof AuthorTp) {
                    AuthorTp author = (AuthorTp) ao;
                    String orgName = "";
                    try {
                        List<OrganizationTp> orgList = a.getOrganization();
                        for (OrganizationTp sot : orgList) {
                            List<Serializable> sList = sot.getContent();
                            for (int i = 0; i < sList.size(); i++) {
                                Serializable serial = sList.get(i);
                                if (serial instanceof JAXBElement) {
                                    JAXBElement<OrganizationTp> sor = (JAXBElement<OrganizationTp>) serial;
                                    orgName += ((OrganizationTp) sor.getValue()).getContent().get(0);
                                } else {
                                    orgName += serial.toString();
                                }
                                orgName += " ";
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    /*2020.03.18 author instance id 추가 coreawin*/
                    String authorInstanceID = author.getAuthorInstanceId();
                    author.getOrcid();

                    String authorID = author.getAuid();
                    String indexName = author.getIndexedName();
                    String givenName = author.getGivenName();
                    String surName = author.getSurname();
                    String initials = author.getInitials();
                    String nameText = author.getNametext();

                    String orcId = author.getOrcid();
                    if(orcId!=null){
                        this.existOrcId = true;
                    }

                    String email = null;
                    try {
                        email = author.getEAddress().getContent();
                    } catch (Exception e) {

                    }
                    if (authorID == null) {
                        authorID = "-1";
                    }
                    // continue;

                    Author author_data = new Author(authorID, indexName, email, givenName, surName, initials, nameText, authorInstanceID, orcId);
                    // System.out.println(author_data);
                    // if (checkDuplication.contains(authorID + indexName) ==
                    // false)
                    // {
                    // if (author.getEAddress() != null) {
                    // correspondAuthor.add(new CorrespondAuthorTable(
                    // document, authorName));
                    // }
                    author_list.add(author_data);
                    authorgroup_list.add(new Author_Group_TP((group_seq + 1), 1, orgName, author.getSeq(), 0, author_data));
                    // checkDuplication.add(authorID + indexName);
                    // } else {
                } else if (ao instanceof CollaborationTp) {

                    CollaborationTp col = (CollaborationTp) ao;
                    String indexName = col.getIndexedName();
                    Text textTy = col.getText();
                    String text = "";
                    List<Serializable> sList = textTy.getContent();
                    for (int i = 0; i < sList.size(); i++) {
                        Serializable serial = sList.get(i);
                        if (serial instanceof JAXBElement) {
                            Object obj = ((JAXBElement) serial).getValue();
                            if (obj instanceof InfTp) {
                                text += ((InfTp) obj).getContent().get(0);
                            } else if (obj instanceof SupTp) {
                                text += ((SupTp) obj).getContent().get(0);
                            }
                        } else {
                            text += serial.toString();
                        }
                        text += " ";
                    }
                    Collaboration coll = new Collaboration((group_seq + 1), indexName, text);
                    collaborationList.add(coll);
                }
                // }
            }

        }
    }

    public class Author_Group_TP {
        private int group_seq;
        private int author_seq;
        private String organization;
        private String ranking;
        private int org_seq;
        private Author author;

        public Author_Group_TP(int gSeq, int aSeq, String orgName, String ranking, int orgSeq, Author _author) {
            this.group_seq = gSeq;
            this.author_seq = aSeq;
            this.organization = orgName;
            this.ranking = ranking;
            this.org_seq = orgSeq;
            this.author = _author;
        }

        public int getGroup_seq() {
            return group_seq;
        }

        public int getAuthor_seq() {
            return author_seq;
        }

        public String getOrganization() {
            return organization;
        }

        public String getRanking() {
            return ranking;
        }

        public int getOrg_seq() {
            return org_seq;
        }

        public Author getAuthor() {
            return author;
        }

    }

    public class Author {
        private String author_id;
        private String author_instance_id;
        private String orc_id;
        private String name;
        private String initials;
        private String indexed_name;
        private String surname;
        private String given_name;
        private String email;

        public Author(String indexName) {
            this.indexed_name = indexName;
        }

        /**
         * author_instance id를 추가했으므로 이 함수는 사용권장하지 않는다.
         *
         * @author neon
         * @since 2022.03.18
         * */
        @Deprecated
        public Author(String authorID, String indexName, String email, String givenName, String surName2, String initials2, String nameText) {
            this.author_id = authorID;
            this.name = indexName;
            this.email = email;
            this.given_name = givenName;
            this.indexed_name = indexName;
            this.surname = surName2;
            this.initials = initials2;
            // System.out.println("=================");
            // System.out.println("name " + name);
            // System.out.println("indexed_name " + indexed_name);
            // System.out.println("email " + email);
            // System.out.println("given_name " + given_name);
        }


        /**
         * author instance id 추가함.
         * @since 2022.03.18
         * @author neon
         * */
        public Author(String authorID, String indexName, String email, String givenName, String surName2, String initials2, String nameText, String author_instance_id) {
            this.author_id = authorID;
            this.name = indexName;
            this.email = email;
            this.given_name = givenName;
            this.indexed_name = indexName;
            this.surname = surName2;
            this.initials = initials2;
            this.author_instance_id = author_instance_id;
            // System.out.println("=================");
            // System.out.println("name " + name);
            // System.out.println("indexed_name " + indexed_name);
            // System.out.println("email " + email);
            // System.out.println("given_name " + given_name);
        }

        /**
         * author instance id 추가함.
         * @since 2023.01.25
         * @author neon
         * */
        public Author(String authorID, String indexName, String email, String givenName, String surName2, String initials2, String nameText, String author_instance_id, String orcId) {
            this.author_id = authorID;
            this.name = indexName;
            this.email = email;
            this.given_name = givenName;
            this.indexed_name = indexName;
            this.surname = surName2;
            this.initials = initials2;
            this.author_instance_id = author_instance_id;
            this.orc_id = orcId;
            // System.out.println("=================");
            // System.out.println("name " + name);
            // System.out.println("indexed_name " + indexed_name);
            // System.out.println("email " + email);
            // System.out.println("given_name " + given_name);
        }

        public String getInitials() {
            return initials;
        }

        public String getIndexed_name() {
            return indexed_name;
        }

        public String getSurname() {
            return surname;
        }

        public String getGiven_name() {
            return given_name;
        }

        public String getAuthor_id() {
            return author_id;
        }

        public void setAuthor_id(String author_id) {
            this.author_id = author_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAuthorInstanceId() {
            return this.author_instance_id;
        }

        public void setAuthorInstanceId(String author_instance_id) {
            this.author_instance_id = author_instance_id;
        }

        public String getOrc_id() {
            return this.orc_id;
        }

        public void setOrc_id(String orc_id) {
            this.orc_id = orc_id;
        }

    }

    public class Collaboration {

        private String text;
        private String indexName;
        private int groupSeq;

        public Collaboration(int groupSeq, String indexName, String text) {
            this.setGroupSeq(groupSeq);
            this.indexName = indexName;
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public int getGroupSeq() {
            return groupSeq;
        }

        public void setGroupSeq(int groupSeq) {
            this.groupSeq = groupSeq;
        }
    }

    public String getEid() {
        return eid;
    }

    public boolean existOrcId() {
        return this.existOrcId;
    }

    public LinkedList<Author_Group_TP> getAuthorgroup_list() {
        return authorgroup_list;
    }

    public LinkedList<Author> getAuthor_list() {
        return author_list;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        sb.append(getClass().getSimpleName());
        sb.append(">");

        LinkedList<Author_Group_TP> authorGroupList = getAuthorgroup_list();
        sb.append("[eid:");
        sb.append(getEid());
        sb.append("]\n");
        for (int idx = 0; idx < authorGroupList.size(); idx++) {
            Author_Group_TP group = authorGroupList.get(idx);
            sb.append("\t[groupSequence:");
            sb.append(group.getGroup_seq());
            sb.append("]");
            sb.append("[Organization:");
            sb.append(group.getOrganization());
            sb.append("]");
            sb.append("[ranking:");
            sb.append(group.getRanking());
            sb.append("]");
            sb.append("[author_id:");
            sb.append(group.getAuthor().getAuthor_id());
            sb.append("]");
            sb.append("[author_orcid:");
            sb.append(group.getAuthor().getOrc_id());
            sb.append("]");
            sb.append("[author_name:");
            sb.append(group.getAuthor().getName());
            sb.append("]\n");
        }
        return sb.toString();
    }


    private final String TAB = "\t";
    private final String ENTER = "\n";
    /**
     * 저자이름 | 저장 ID | Occid
     * @return
     * @since 2023-01-25
     * @author coreawin
     */
    public String toString4최현규() {
        StringBuffer sb = new StringBuffer();
        LinkedList<Author>  authorList = getAuthor_list();
        for(Author a : authorList){
            String orc_id = a.orc_id;
            if(orc_id!=null){
                sb.append(a.getName());
                sb.append(TAB);
                sb.append(a.getAuthor_id());
                sb.append(TAB);
                sb.append(a.getOrc_id());
                sb.append(ENTER);
            }
        }
        return sb.toString();
    }

    public LinkedList<Collaboration> getCollaborationList() {
        return collaborationList;
    }
}
