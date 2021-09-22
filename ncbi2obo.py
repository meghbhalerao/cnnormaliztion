import sys
import os
sys.path.insert(0, "/Users/megh/work/cnnormaliztion")
from code.dataset import *

dataset_name  = 'mp'
name_array, query_id_array, mention2id, _ = load_data(filename=os.path.join("./data/obo-data/%s.obo"%(dataset_name)))

queries_train, queries_test, queries_valid = data_split(query_id_array,is_unseen=False,test_size = 0.33)

name2id, query2id = process_data_dict(name_array, query_id_array, mention2id)

if not os.path.exists("./data/%s_data/"%(dataset_name)):
    os.mkdir(os.path.join("./data/%s_data/"%(dataset_name)))

file_t  = open("./data/%s_data/TERMINOLOGY.txt"%(dataset_name),"w")

for idx, name_ in enumerate(name2id.keys()):
    file_t.write("C" + str(name2id[name_]) + "||" + str(name_) + "\n")

for split in ['training','test','dev']:
    if not os.path.exists(os.path.join("./data/%s_data/"%(dataset_name))):
        os.mkdir(os.path.join("./data/%s_data/"%(dataset_name)))

    if not os.path.exists(os.path.join("./data/%s_data/%s/"%(dataset_name,split))):
        os.mkdir(os.path.join("./data/%s_data/%s/"%(dataset_name,split)))

    data_dir =  os.path.join("./data/%s_data/%s/"%(dataset_name, split))

    if split=="training":
        query_iter = queries_train
    elif split == "dev":
        query_iter = queries_valid
    elif split == "test":
        query_iter = queries_test

    for idx, query in enumerate(query_iter):
        if len(query) ==  1:
            continue
        pseudo_id = 9999 + idx
        f_temp =  open(os.path.join(data_dir,str(pseudo_id) + ".concept"),"w")
        f_temp.write(str(pseudo_id) + "||" + "0|0||%s||"%(dataset_name) + str(query) + "||C" + str(query2id[query]) + "\n")
        f_text =  open(os.path.join(data_dir,str(pseudo_id) + ".txt"),"w")
        f_text.write(str(query))
        f_text.close()
        f_temp.close()
    