module Convert where

import Foreign
import Data.Char
import Foreign.C.Types
import Foreign.C.String
import Data.Foldable (foldr')
import Data.ByteString (ByteString,pack,unpack)
import Data.ByteString.Char8 (readInt)
import Data.Double.Conversion.ByteString(toShortest,toFixed)
foreign import ccall unsafe "string.h strlen" c_strlen :: CString -> IO Int

toStr::ByteString->String
toStr bs= snd $ foldr' foldFun (0,[]) $ unpack bs 

toInt::ByteString->Int
toInt str=case readInt str of
  Just (r,s)->r
  Nothing->error "Can not convert  to Int"

toDouble::ByteString->Double
toDouble= read . toStr 

fromStr::String->ByteString
fromStr s= pack $ foldr' (fWord.ord) [] s 

fromInt :: Int -> ByteString
fromInt = fromStr . show

fromDouble::Double->ByteString
fromDouble=toShortest

fromDouble2::Double->ByteString
fromDouble2 =toFixed 2

fromDouble3::Double->ByteString
fromDouble3 =toFixed 3

newCWStr::ByteString->IO CWString
newCWStr =newCWString . toStr

peekCWStr::CWString->IO ByteString
peekCWStr cws=do
  str<-peekCWString cws
  let res=fromStr $! str
  return res 

newCStrUtf8::String->IO CString 
newCStrUtf8 s= newArray $ foldr' (fCod . ord) [] s ++ [0]    

peekUt8Str::CString->IO String
peekUt8Str cs=do
  l<-c_strlen cs
  myCs' cs (l-1) (0,[])  where
  myCs' cs n (prev,lst)=do
    res<-peek (cs `plusPtr` n)::IO Word8
    if n<0 then return lst else  myCs' cs (n-1) (foldFun  res (prev,lst) )

fCod::Int->[CChar]->[CChar]
fCod el ac
      | el<255    =CChar (fromIntegral(el::Int)::Int8) : ac
      | el==1030  = 208:0x86:ac
      | el==1110  = 209:0x96:ac
      | el==1031  = 208:0x87:ac
      | el==1111  = 209:0x97:ac
      | el==1111  = 208:0x84:ac
      | el==1108  = 209:0x94:ac
      | el<1088   = 208:CChar(fromIntegral(el-896::Int)::Int8):ac
      | el<1104   = 209:CChar(fromIntegral(el-960::Int)::Int8):ac
      | el==1105  = 209:0x91:ac
      | el==1168  = 210:0x90:ac
      | el==1169  = 210:0x91:ac
      | otherwise =error $ show el

fWord::Int->[Word8]->[Word8]
fWord el ac
      | el<255    = (fromIntegral(el::Int)::Word8) : ac
      | el==1030  = 208:0x86:ac
      | el==1110  = 209:0x96:ac
      | el==1031  = 208:0x87:ac
      | el==1111  = 209:0x97:ac
      | el==1111  = 208:0x84:ac
      | el==1108  = 209:0x94:ac
      | el<1088   = 208:(fromIntegral(el-896::Int)::Word8):ac
      | el<1104   = 209:(fromIntegral(el-960::Int)::Word8):ac
      | el==1105  = 209:0x91:ac
      | el==1168  = 210:0x90:ac
      | el==1169  = 210:0x91:ac
      | otherwise =error $ "unknown symbol=" ++ show el      

foldFun  el (prev, lst)
 | el < 128 = (0, chr (fromIntegral (el :: Word8) :: Int) : lst)
 | el == 208  = (0, chr (896+fromIntegral (prev :: Word8) :: Int) : lst)
 | el == 209  = (0, chr (960+fromIntegral (prev :: Word8) :: Int) : lst)
 | el == 210  = (0, chr (1024+fromIntegral (prev :: Word8) :: Int) : lst)
 | otherwise = (el, lst)      

cStrUtf8AndLen::String->IO (Int, Ptr CChar)
cStrUtf8AndLen s=do
  let lst=foldr' (fCod . ord) [] s 
  ar<-newArray lst
  return (length lst,ar )  

---------------------------------------------------------------------
--import qualified Data.Text as T
--import qualified Data.Text.Encoding as E
--import Data.Text.Lazy (toStrict)
--import Data.Text.Lazy.Builder (toLazyText)
--import qualified Data.Text.Lazy.Builder.Int as D (decimal)
--import Data.Text.Read
--import Data.ByteString.Lex.Integral

--toStr'::ByteString->String
--toStr' =T.unpack . E.decodeUtf8

--fromStr'::String->ByteString
--fromStr' = E.encodeUtf8 . T.pack

--peekUt8Str'::Addr->Int->IO String
--peekUt8Str' cs l=
--  myCs' cs l (0,[])  where
--  myCs' cs n (prev,lst)=do
--    res<-peek (cs `plusPtr` n)::IO Word8
--    if n<0 then return lst else  myCs' cs (n-1) (foldFun (prev,lst) res)
--  foldFun (prev, lst) el
--    | el < 128 = (0, chr (fromIntegral (el :: Word8) :: Int) : lst)
--    | el == 208  = (0, chr (896+fromIntegral (prev :: Word8) :: Int) : lst)
--    | el == 209  = (0, chr (960+fromIntegral (prev :: Word8) :: Int) : lst)
--    | el == 210  = (0, chr (1024+fromIntegral (prev :: Word8) :: Int) : lst)
--    | otherwise = (el, lst)
--
 
 --toInt_::ByteString->Int
 --toInt_ =readDecimal_
 
 --toDouble::ByteString->Double
 --toDouble s=case double $ E.decodeUtf8 s of
 --  Right (r,s)->r
 --  Left r->error r
 
 --fromInt :: Int -> B.ByteString
 --fromInt = E.encodeUtf8 . toStrict. toLazyText . D.decimal    