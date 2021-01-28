package com.gitee.search.models;

import com.gitee.search.core.Constants;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;

import java.io.Serializable;

/**
 * Searchable object
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class Searchable implements Serializable {

    protected long id;      // object id , ex: repo id, issue id
    protected int _id;      // document id
    protected float _score; // document score

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public float get_score() {
        return _score;
    }

    public void set_score(float _score) {
        this._score = _score;
    }

    protected Document newDocument() {
        Document doc = new Document();
        doc.add(new StringField(Constants.FIELD_ID, String.valueOf(id), Field.Store.YES));
        doc.add(new NumericDocValuesField(Constants.FIELD_ID, id));
        return doc;
    }

    /**
     * generate lucene document
     * @return
     */
    public abstract Document getDocument() ;

    /**
     * Read fields from document
     * @param doc
     */
    public abstract Searchable setDocument(Document doc) ;

    /**
     * 读取数值字段
     * @param doc
     * @param fieldName
     * @param defValues
     * @return
     */
    public final static int getIntField(Document doc, String fieldName, int...defValues) {
        int def = (defValues.length > 0)?defValues[0]:-1;
        return NumberUtils.toInt(doc.get(fieldName), def);
    }

}