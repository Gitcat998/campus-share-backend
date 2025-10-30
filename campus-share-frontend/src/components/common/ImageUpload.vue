<template>
  <div class="image-upload">
    <el-upload
        :action="uploadUrl"
        list-type="picture-card"
        :on-success="handleSuccess"
        :on-remove="handleRemove"
        :limit="maxCount"
        :on-exceed="handleExceed"
        :file-list="fileList"
        :on-preview="handlePictureCardPreview"
        :headers="uploadHeaders"
        :data="uploadParams"
    name="files"
    :auto-upload="true"
    >
    <i class="el-icon-plus"></i>
    </el-upload>
    <el-dialog
        :visible.sync="dialogVisible"
        title="预览"
        width="80%"
    >
      <img
          :src="dialogImageUrl"
          alt=""
          style="width: 100%;"
      >
    </el-dialog>
  </div>
</template>

<script>
import request from '../../utils/request'
import { getToken } from '../../utils/auth'
export default {
  name: 'ImageUpload',
  props: {
    itemId: {
      type: Number,
      required: true,
      validator: (value) => {
        if (value === null || value === undefined) {
          console.error('错误：必须传入itemId（物品ID）')
          return false
        }
        return true
      }
    },
    mainImageIndex: {
      type: Number,
      default: 0
    },
    maxCount: {
      type: Number,
      default: 5
    }
  },
  data() {
    return {
      uploadUrl: `${request.defaults.baseURL}/images/upload`,
      fileList: [],
      dialogVisible: false,
      dialogImageUrl: ''
    }
  },
  computed: {
    uploadHeaders() {
      const token = getToken()
      return {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    },
    // 关键修复：将uploadParams移到computed中（之前放在了错误的位置）
    uploadParams() {
      return {
        itemId: this.itemId,
        mainImageIndex: this.mainImageIndex
      }
    }
  },
  methods: {
    handleSuccess(response, file) {
      if (response.code === 200) {
        file.imageId = response.data.imageId
        file.url = response.data.imageUrl
        this.fileList.push(file)
        this.notifyParent()
      } else {
        this.$message.error(response.message || '上传失败')
      }
    },
    handleRemove(file) {
      this.fileList = this.fileList.filter(f => f.uid !== file.uid)
      this.notifyParent()
    },
    handleExceed() {
      this.$message.warning(`最多只能上传 ${this.maxCount} 张图片`)
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    notifyParent() {
      this.$emit('change', this.fileList.map(file => ({
        imageId: file.imageId,
        imageUrl: file.url
      })))
    }
  }
}
</script>

<style scoped>
.image-upload {
  margin-top: 10px;
}
</style>