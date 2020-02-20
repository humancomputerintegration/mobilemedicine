from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
from Crypto.Cipher import AES
import base64
import random 

def encrypt_siv(key, raw_msg):
    header = b'header'
    data = raw_msg.encode()
    cipher = AES.new(key, AES.MODE_SIV)
    # cipher.update(header)
    ctxt, tag = cipher.encrypt_and_digest(data)
    # json_k = [ 'header', 'ciphertext', 'tag' ]
    # json_v = [ base64.b64encode(x).decode('utf-8') for x in json_k ]
    # result = json.dumps(dict(zip(json_k, json_v)))
    return base64.b64encode(ctxt), base64.b64encode(tag)

def decrypt_siv(key, enc_msg, tag):
    cipher = AES.new(key, AES.MODE_SIV)
    # cipher.update(header)
    mod_enc_msg = base64.b64decode(enc_msg)
    mod_tag = base64.b64decode(tag)
    ptxt = cipher.decrypt_and_verify(mod_enc_msg, mod_tag)
    return ptxt


def generate_keys(public_file='public.pem',private_file='private.pem', bsize=2048):
    gen_key = RSA.generate(bsize)
    prikey = gen_key.export_key()
    file_out = open(private_file, "wb")
    file_out.write(prikey)

    pubkey = gen_key.publickey().export_key()
    file_out = open(public_file, "wb")
    file_out.write(pubkey)
    return gen_key


def decrypt(enc_msg, pkeyf= 'private.pem'):
    prikey= RSA.import_key(open(pkeyf, 'rb').read())
    decrypter= PKCS1_OAEP.new(key=prikey) 
    dec_msg = decrypter.decrypt(enc_msg)
    return dec_msg 

def encrypt(raw_msg, pubkeyf= 'public.pem'):
    pubkey= RSA.import_key(open(pubkeyf, 'rb').read())
    encrypter = PKCS1_OAEP.new(key=pubkeyf)
    enc_msg = encrypter.encrypt(raw_msg)
    return enc_msg

#The reason for smaller sized keys is for faster encryption/decryption
#in addition to being able to fit under the text limit of SMS 
# however, this comes at the expense of security (not to a serious extent)
def testing(test_strings, write_new_keys=False, bsize=1024):
    print("Testing encryption module with some test_strings")
    print("------------------------------------------------")

    enc_str,ctxts,dmsg = [],[],[]

    for s in test_strings: 
        enc_str.append(s.encode('utf-8'))

    print(enc_str)

    if(write_new_keys):
        bkey = generate_keys('public.pem','private.pem',bsize=bsize)
        prik = RSA.import_key(open("private.pem",'r').read())
        pubk = RSA.import_key(open("public.pem",'r').read())
    else: 
        gen_key = RSA.generate(bsize)
        prik = RSA.import_key(gen_key.export_key())
        pubk = RSA.import_key(gen_key.publickey().export_key())

    # print(private_pem)
    # print(public_pem)
    cipher = PKCS1_OAEP.new(key=pubk)
    for ind,es in enumerate(enc_str):
        # tempc = base64.a85encode(cipher.encrypt(es))
        tempc = base64.b64encode(cipher.encrypt(es))
        # tempc = cipher.encrypt(es)
        print(tempc)
        print("encrypted msg length {} = {}".format(ind, len(tempc)))
        ctxts.append(tempc)

    decrypt = PKCS1_OAEP.new(key=prik)
    for ct in ctxts:
        # tmpd = decrypt.decrypt(base64.a85decode(ct))
        tmpd = decrypt.decrypt(base64.b64decode(ct))
        # tmpd = decrypt.decrypt(ct)
        dmsg.append(tmpd)

    for index,result in enumerate(dmsg): 
        assert result ==  enc_str[index]
    print("------------------------------------------------")
    print("Passed")
    return True

def testing_SIV(test_strings, key):
    print("Testing encryption SIV module with some test_strings")
    print("----------------------------------------------------")

    ctxts,dmsg = [],[]

    # cipher = AES.new(key, AES.MODE_SIV)
    for ind,es, in enumerate(test_strings):
        tempc = encrypt_siv(key, es)
        print(tempc)
        print("encrypted msg length {} = {}".format(ind, len(tempc[0])))
        ctxts.append(tempc)


    for (ct,tag) in ctxts:
        tmpd = decrypt_siv(key, ct, tag)
        print(tmpd)
        dmsg.append(tmpd)

    for index,result in enumerate(dmsg): 
        assert result.decode() ==  test_strings[index]
    print("------------------------------------------------")
    print("Passed")
    return True

if __name__ == "__main__":
    test_strings = []
    test_strings.append("this is a test message")
    test_strings.append("test message")
    test_strings.append('''this is a test message and I am testing if this works \\ 
        with respect''')

    # testing(test_strings, write_new_keys = False)

    key = b'0' * 32
    # test_enc, test_header, test_tag = encrypt_siv(test_strings[1], key)
    # test2 = decrypt_siv(test_enc,key,test_header,test_tag)

    testing_SIV(test_strings, key)

    
